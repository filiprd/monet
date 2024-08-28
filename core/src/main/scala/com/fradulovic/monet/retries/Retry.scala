package com.fradulovic.monet.retries

import cats.effect.Temporal
import org.typelevel.log4cats.Logger
import retry.*
import retry.RetryDetails.{GivingUp, WillDelayAndRetry}

trait Retry[F[_]] {

  /** Retries execution of a given effect according to a given retry policy
    * @param retryPolicy
    *   Policy to follow when making retries
    * @param retriable
    *   Action being retried
    * @param fa
    *   Effect to retry
    * @return
    */
  def retry[A](retryPolicy: RetryPolicy[F], retriable: Retriable)(fa: F[A]): F[A]
}

object Retry {
  def apply[F[_]: Retry]: Retry[F] = summon[Retry[F]]

  given myRetry[F[_]: Logger: Temporal]: Retry[F] = new Retry[F] {
    def retry[A](retryPolicy: RetryPolicy[F], retriable: Retriable)(fa: F[A]): F[A] = {

      /** Our custom error handling method for graceful logging of retry process
        */
      def onError(err: Throwable, details: RetryDetails): F[Unit] = details match {
        case WillDelayAndRetry(_, retriesSoFar: Int, _) =>
          Logger[F].error(
            s"Failed to process $retriable with ${err.getMessage}. So far we have retried $retriesSoFar times."
          )
        case GivingUp(totalRetries: Int, _) =>
          Logger[F].error(s"Giving up on $retriable after $totalRetries retries")
      }

      retryingOnAllErrors[A](retryPolicy, onError)(fa) // calling cats-retry
    }
  }
}
