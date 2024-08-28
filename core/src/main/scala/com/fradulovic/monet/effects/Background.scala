package com.fradulovic.monet.effects

import scala.concurrent.duration.FiniteDuration

import cats.effect.Temporal
import cats.effect.std.Supervisor
import cats.syntax.all.*

trait Background[F[_]] {

  /** Schedules a given effect to be executed after specified time duration
    * @param fa
    *   Effect to execute
    * @param duration
    *   Time duration to pass before effect execution
    * @return
    */
  def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit]
}

object Background {
  def apply[F[_]: Background]: Background[F] = summon[Background[F]]

  given backgroundInstance[F[_]](using S: Supervisor[F], T: Temporal[F]): Background[F] = new Background[F] {
    def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] =
      S.supervise(T.sleep(duration) >> fa).void
  }
}
