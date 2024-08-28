package com.fradulovic.monet.effects

import com.fradulovic.monet.retries.{Retriable, Retry}

import cats.effect.IO
import cats.effect.kernel.Ref
import retry.*
import retry.RetryDetails.*

object TestRetry {

  def givingUp(ref: Ref[IO, Option[GivingUp]]): Retry[IO] = new Retry[IO] {
    def retry[T](policy: RetryPolicy[IO], retriable: Retriable)(fa: IO[T]): IO[T] = {
      def onError(e: Throwable, details: RetryDetails): IO[Unit] = details match {
        case g: GivingUp => ref.set(Some(g))
        case _           => IO.unit
      }
      retryingOnAllErrors[T](policy, onError)(fa)
    }
  }

  def recovering(ref: Ref[IO, Option[WillDelayAndRetry]]): Retry[IO] = new Retry[IO] {
    def retry[T](policy: RetryPolicy[IO], retriable: Retriable)(fa: IO[T]): IO[T] = {
      def onError(e: Throwable, details: RetryDetails): IO[Unit] = details match {
        case a: WillDelayAndRetry => ref.set(Some(a))
        case _                    => IO.unit
      }
      retryingOnAllErrors[T](policy, onError)(fa)
    }
  }
}
