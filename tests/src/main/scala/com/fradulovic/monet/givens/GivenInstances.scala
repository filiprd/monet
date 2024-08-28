package com.fradulovic.monet.givens

import scala.concurrent.duration.FiniteDuration

import com.fradulovic.monet.effects.Background

import cats.effect.IO

object GivenInstances {

  given noOpBackground: Background[IO] = new Background[IO] {
    override def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] = IO.unit
  }
}
