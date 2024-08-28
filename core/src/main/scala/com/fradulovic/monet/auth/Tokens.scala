package com.fradulovic.monet.auth

import com.fradulovic.monet.conf.ConfigTypes.*
import com.fradulovic.monet.effects.GenUUID

import cats.Monad
import cats.syntax.all.*
import dev.profunktor.auth.jwt.{JwtSecretKey, JwtToken, jwtEncode}
import io.circe.syntax.*
import pdi.jwt.{JwtAlgorithm, JwtClaim}

trait Tokens[F[_]] {
  def create: F[JwtToken]
}

object Tokens {
  def make[F[_]: GenUUID: Monad](
      jwtExpire: JwtExpire[F],
      config: JwtAccessTokenKeyConfig,
      exp: TokenExpiration
  ): Tokens[F] =
    new Tokens[F] {
      def create: F[JwtToken] =
        for {
          uuid     <- GenUUID[F].make
          claim    <- jwtExpire.expiresIn(JwtClaim(uuid.asJson.noSpaces), exp)
          secretKey = JwtSecretKey(config.value)
          token    <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
        } yield token
    }
}
