package com.fradulovic.monet.http.clients

import java.net.URI

import com.fradulovic.monet.conf.ConfigTypes.PaymentClientUri
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.domain.{Payment, PaymentError}
import com.fradulovic.monet.gens.MonetGenerators.*

import cats.effect.IO
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.{HttpRoutes, Response}
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object PaymentClientTest extends SimpleIOSuite with Checkers {

  private val dummyPaymentUri = PaymentClientUri(URI("http://localhost/payments"))

  val gen: Gen[(PaymentId, Payment)] = for {
    pid <- idGen[PaymentId]
    p   <- paymentGen
  } yield (pid, p)

  def mkRoutes(res: IO[Response[IO]]) =
    HttpRoutes
      .of[IO] { case POST -> Root / "payments" => res }
      .orNotFound

  test("successful payment") {
    forall(gen) { case (pid, p) =>
      val client = Client.fromHttpApp(mkRoutes(Ok(pid)))
      PaymentClient
        .make[IO](dummyPaymentUri, client)
        .processPayment(p)
        .map(expect.same(pid, _))
    }
  }

  test("conflict 409 in payment") {
    forall(gen) { case (pid, p) =>
      val client = Client.fromHttpApp(mkRoutes(Conflict(pid)))
      PaymentClient
        .make[IO](dummyPaymentUri, client)
        .processPayment(p)
        .map(expect.same(pid, _))
    }
  }

  test("failed payment") {
    forall(gen) { case (pid, p) =>
      val client = Client.fromHttpApp(mkRoutes(InternalServerError()))
      PaymentClient
        .make[IO](dummyPaymentUri, client)
        .processPayment(p)
        .attempt
        .map {
          case Left(e)  => expect.same(PaymentError("Internal Server Error"), e)
          case Right(_) => failure("expected payment error but was successful")
        }
    }
  }
}
