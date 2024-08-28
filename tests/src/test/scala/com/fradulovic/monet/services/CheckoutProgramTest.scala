package com.fradulovic.monet.services

import scala.concurrent.duration.*

import com.fradulovic.monet.conf.ConfigTypes.OrderCreationBackoff
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.{PaymentId, UserId}
import com.fradulovic.monet.effects.{Background, TestBackground, TestRetry}
import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.interpreters.{CartServiceInterpreters, OrderServiceInterpreters, PaymentClientInterpreters}
import com.fradulovic.monet.programs.CheckoutProgram
import com.fradulovic.monet.retries.Retry

import cats.effect.{IO, Ref}
import cats.syntax.all.*
import org.scalacheck.Gen
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import retry.RetryDetails.{GivingUp, WillDelayAndRetry}
import retry.{RetryPolicies, RetryPolicy}
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object CheckoutProgramTest extends SimpleIOSuite with Checkers {

  import com.fradulovic.monet.givens.GivenInstances.noOpBackground
  given lg: SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  val MaxRetries                   = 3
  val retryPolicy: RetryPolicy[IO] = RetryPolicies.limitRetries[IO](MaxRetries)
  val orderBackoff                 = OrderCreationBackoff(1.hour)

  val gen = for {
    uid  <- idGen[UserId]
    ords <- Gen.nonEmptyListOf(orderGen)
    cc   <- creditCardGen
    pid  <- idGen[PaymentId]
    p    <- Gen.nonEmptyListOf(paintingGen)
    pr   <- priceGen
  } yield (uid, ords, cc, pid, p.toSet, pr)

  test("successful checkout") {
    forall(gen) { case (uid, ords, cc, pid, p, pr) =>
      CheckoutProgram[IO](
        CartServiceInterpreters.successfulCartService(Cart(uid, p, pr)),
        PaymentClientInterpreters.successfulPaymentClient(pid),
        OrderServiceInterpreters.successfulOrderService(ords),
        retryPolicy,
        orderBackoff
      )
        .checkout(uid, cc)
        .map(expect.same(ords.head.uuid, _))
    }
  }

  test("empty cart") {
    forall(gen) { case (uid, ords, cc, pid, p, pr) =>
      CheckoutProgram[IO](
        CartServiceInterpreters.emptyCartService(),
        PaymentClientInterpreters.successfulPaymentClient(pid),
        OrderServiceInterpreters.successfulOrderService(ords),
        retryPolicy,
        orderBackoff
      )
        .checkout(uid, cc)
        .attempt
        .map {
          case Left(EmptyCartError) => success
          case Right(_)             => failure("Cart not empty")
        }
    }
  }

  test("failing payment client") {
    forall(gen) { case (uid, ords, cc, pid, p, pr) =>
      Ref.of[IO, Option[GivingUp]](None).flatMap { retries =>
        given rt: Retry[IO] = TestRetry.givingUp(retries)

        CheckoutProgram[IO](
          CartServiceInterpreters.successfulCartService(Cart(uid, p, pr)),
          PaymentClientInterpreters.failingPaymentClient(),
          OrderServiceInterpreters.successfulOrderService(ords),
          retryPolicy,
          orderBackoff
        )
          .checkout(uid, cc)
          .attempt
          .flatMap {
            case Left(PaymentError(_)) =>
              retries.get.map {
                case Some(g) => expect.same(g.totalRetries, MaxRetries)
                case None    => failure("expected GivingUp")
              }
            case _ => IO.pure(failure("Expected payment error"))
          }
      }
    }
  }

  test("recovering payment client") {
    forall(gen) { case (uid, ords, cc, pid, p, pr) =>
      (Ref.of[IO, Int](0), Ref.of[IO, Option[WillDelayAndRetry]](None)).tupled.flatMap { case (ref, retries) =>
        given rt: Retry[IO] = TestRetry.recovering(retries)

        CheckoutProgram[IO](
          CartServiceInterpreters.successfulCartService(Cart(uid, p, pr)),
          PaymentClientInterpreters.recoveringPaymentClient(ref, pid),
          OrderServiceInterpreters.successfulOrderService(ords),
          retryPolicy,
          orderBackoff
        )
          .checkout(uid, cc)
          .attempt
          .flatMap {
            case Right(id) =>
              retries.get.map {
                case Some(a) => expect.same(a.retriesSoFar, 1) and expect.same(id, ords.head.uuid)
                case None    => failure("expected success on the second try")
              }
            case Left(PaymentError(_)) => IO.pure(failure("recovering client did not succeed"))
          }
      }
    }
  }

  test("failing orders") {
    forall(gen) { case (uid, ords, cc, pid, p, pr) =>
      (
        Ref.of[IO, (Int, FiniteDuration)](0 -> 0.seconds),
        Ref.of[IO, Option[GivingUp]](None)
      ).tupled.flatMap { case (acc, retries) =>
        given counterBackground: Background[IO] = TestBackground.backgroundCounter(acc)
        given rt: Retry[IO]                     = TestRetry.givingUp(retries)

        CheckoutProgram[IO](
          CartServiceInterpreters.successfulCartService(Cart(uid, p, pr)),
          PaymentClientInterpreters.successfulPaymentClient(pid),
          OrderServiceInterpreters.failingOrderService(),
          retryPolicy,
          orderBackoff
        )
          .checkout(uid, cc)
          .attempt
          .flatMap {
            case Left(OrderError(_)) =>
              (acc.get, retries.get).mapN {
                case (c, Some(g)) =>
                  expect.same(c, 1 -> 1.hour) and
                    expect.same(g.totalRetries, MaxRetries)
                case _ => failure(s"Expected $MaxRetries retries and reschedule")
              }
            case _ => IO.pure(failure("Expected order error"))
          }
      }
    }
  }

  test("failing cart deletion") {
    forall(gen) { case (uid, ords, cc, pid, p, pr) =>
      CheckoutProgram[IO](
        CartServiceInterpreters.failingCartDeletionService(Cart(uid, p, pr)),
        PaymentClientInterpreters.successfulPaymentClient(pid),
        OrderServiceInterpreters.successfulOrderService(ords),
        retryPolicy,
        orderBackoff
      )
        .checkout(uid, cc)
        .map(expect.same(ords.head.uuid, _))
    }
  }
}
