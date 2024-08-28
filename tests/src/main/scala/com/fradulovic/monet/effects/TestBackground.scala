package com.fradulovic.monet.effects

import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import cats.effect.kernel.Ref

object TestBackground {

  def backgroundCounter(ref: Ref[IO, (Int, FiniteDuration)]): Background[IO] = new Background[IO] {
    def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] =
      ref.update { case (n, f) => (n + 1, f + duration) }
  }
}
