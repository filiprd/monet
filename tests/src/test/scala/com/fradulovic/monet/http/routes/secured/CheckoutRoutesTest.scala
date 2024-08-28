package com.fradulovic.monet.http.routes.secured

import scala.concurrent.duration.*

import com.fradulovic.monet.conf.ConfigTypes.OrderCreationBackoff
import com.fradulovic.monet.domain.NewTypes.{PaymentId, UserId}
import com.fradulovic.monet.domain.{Cart, CreditCard}
import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.interpreters.{CartServiceInterpreters, OrderServiceInterpreters, PaymentClientInterpreters}
import com.fradulovic.monet.programs.CheckoutProgram

import cats.effect.IO
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.implicits.uri
import org.scalacheck.Gen
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import retry.{RetryPolicies, RetryPolicy}

object CheckoutRoutesTest extends HttpTestSuite {

  val MaxRetries                   = 3
  val retryPolicy: RetryPolicy[IO] = RetryPolicies.limitRetries[IO](MaxRetries)

  import com.fradulovic.monet.givens.GivenInstances.noOpBackground
  given lg: SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  val gen = for {
    au   <- authUserGen
    uid  <- idGen[UserId]
    ords <- Gen.nonEmptyListOf(orderGen)
    cc   <- creditCardGen
    pid  <- idGen[PaymentId]
    p    <- Gen.nonEmptyListOf(paintingGen)
    pr   <- priceGen
  } yield (au, uid, ords, cc, pid, p.toSet, pr)

  test("successful checkout request") {
    forall(gen) { case (au, uid, ords, cc, pid, p, pr) =>
      val successfulCheckoutService = CheckoutProgram[IO](
        CartServiceInterpreters.successfulCartService(Cart(uid, p, pr)),
        PaymentClientInterpreters.successfulPaymentClient(pid),
        OrderServiceInterpreters.successfulOrderService(ords),
        retryPolicy,
        OrderCreationBackoff(1.hour)
      )

      val req    = Request[IO](Method.POST, uri"/checkout").withEntity(cc)
      val routes = CheckoutRoutes[IO](successfulCheckoutService).routes(testAuthMiddleware(au))

      expectStatus(req, routes)(Status.Ok)
    }
  }

}
