package com.fradulovic.monet.modules

import scala.concurrent.duration.FiniteDuration

import com.fradulovic.monet.alg.{CartService, OrderAlg}
import com.fradulovic.monet.conf.CheckoutConfig
import com.fradulovic.monet.conf.ConfigTypes.RetriesLimit
import com.fradulovic.monet.effects.Background
import com.fradulovic.monet.http.clients.PaymentClient
import com.fradulovic.monet.programs.CheckoutProgram
import com.fradulovic.monet.retries.Retry

import cats.MonadThrow
import cats.syntax.all.*
import org.typelevel.log4cats.Logger
import retry.RetryPolicies.*
import retry.RetryPolicy

sealed abstract class Programs[F[_]: Background: Logger: MonadThrow: Retry] private (
    val checkoutProgram: CheckoutProgram[F]
) {}

object Programs {
  def make[F[_]: Background: Logger: MonadThrow: Retry](
      cartService: CartService[F],
      paymentClient: PaymentClient[F],
      orderService: OrderAlg[F],
      config: CheckoutConfig
  ) = {

    val retryPolicy: RetryPolicy[F] =
      limitRetries[F](config.retriesLimit.value) |+| exponentialBackoff[F](config.retriesBackoff.value)

    new Programs[F](
      CheckoutProgram(cartService, paymentClient, orderService, retryPolicy, config.orderCreationBackoff)
    ) {}
  }
}
