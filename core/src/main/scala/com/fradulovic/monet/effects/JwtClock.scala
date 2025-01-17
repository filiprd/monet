package com.fradulovic.monet.effects

import java.time.Clock

import cats.effect.Sync

trait JwtClock[F[_]] {
  def utc: F[Clock]
}

object JwtClock {
  def apply[F[_]: JwtClock]: JwtClock[F] = summon

  given jwtClockInstance[F[_]: Sync]: JwtClock[F] = new JwtClock[F] {
    def utc: F[Clock] = Sync[F].delay(Clock.systemUTC())
  }
}
