package com.fradulovic.monet.auth

import com.fradulovic.monet.conf.ConfigTypes.TokenExpiration
import com.fradulovic.monet.effects.JwtClock

import cats.effect.Sync
import cats.syntax.all.*
import pdi.jwt.JwtClaim

trait JwtExpire[F[_]] {
  def expiresIn(claim: JwtClaim, exp: TokenExpiration): F[JwtClaim]
}

object JwtExpire {
  def make[F[_]: Sync]: F[JwtExpire[F]] =
    JwtClock[F].utc.map { implicit jClock =>
      new JwtExpire[F] {
        def expiresIn(claim: JwtClaim, exp: TokenExpiration): F[JwtClaim] =
          Sync[F].delay(claim.issuedNow.expiresIn(exp.value.toMillis))
      }
    }
}
