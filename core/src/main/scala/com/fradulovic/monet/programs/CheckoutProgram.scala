package com.fradulovic.monet.programs

import scala.collection.immutable.SortedSet
import scala.concurrent.duration.*

import com.fradulovic.monet.alg.{CartService, OrderAlg}
import com.fradulovic.monet.conf.ConfigTypes.OrderCreationBackoff
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.effects.Background
import com.fradulovic.monet.http.clients.PaymentClient
import com.fradulovic.monet.retries.*

import cats.MonadThrow
import cats.data.{NonEmptySet, NonEmptySetImpl}
import cats.syntax.all.*
import org.typelevel.log4cats.Logger
import retry.*

final case class CheckoutProgram[F[_]: Background: Logger: MonadThrow: Retry](
    cartService: CartService[F],
    paymentClient: PaymentClient[F],
    orderService: OrderAlg[F],
    retryPolicy: RetryPolicy[F],
    orderCreationBackoff: OrderCreationBackoff
) {

  private def ensureNonEmpty[A](paintings: Set[A])(using Ordering[A]): F[NonEmptySet[A]] = {
    val sortedSet = collection.immutable.SortedSet[A]() ++ paintings
    MonadThrow[F].fromOption(NonEmptySetImpl.fromSet(sortedSet), EmptyCartError)
  }

  /** We want to retry payment process if it fails */
  private def processPayment(payment: Payment): F[PaymentId] =
    Retry[F]
      .retry(retryPolicy, RetriablePayment)(paymentClient.processPayment(payment))
      .adaptError { case e =>
        PaymentError(Option(e.getMessage).getOrElse("Unknown payment error"))
      }

  /** If storing orders fails, we want to schedule it again and move on */
  private def createOrder(
      buyerId: UserId,
      paintings: NonEmptySet[PaintingId],
      total: Price,
      paymentId: PaymentId
  ): F[OrderId] = {
    def backgroundAction(fa: F[OrderId]): F[OrderId] =
      fa.onError { case _ =>
        Logger[F].error(s"Failed to create order for Payment: ${paymentId.show}. Rescheduling as a background action")
          >> Background[F].schedule(backgroundAction(fa), orderCreationBackoff.value)
      }

    val createOrderAction =
      Retry[F]
        .retry(retryPolicy, RetriableOrder)(orderService.storeOrder(buyerId, paintings, total, paymentId))
        .adaptError { case e =>
          OrderError(e.getMessage)
        }

    backgroundAction(createOrderAction)
  }

  def checkout(userId: UserId, creditCard: CreditCard): F[OrderId] =
    for {
      cart      <- cartService.getCart(userId)
      total      = cart.total
      paintings <- ensureNonEmpty(cart.paintings.map(_.uuid)) // stops if the cart is empty
      paymentId <- processPayment(Payment(userId, total, creditCard))
      orderId   <- createOrder(userId, paintings, total, paymentId)
      _ <- cartService
             .deleteCart(userId)
             .attempt // we don't want checkout to crash if an error occurs during cart deletion
             .void
    } yield orderId
}
