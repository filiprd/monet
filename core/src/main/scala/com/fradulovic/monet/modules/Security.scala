package com.fradulovic.monet.modules

import com.fradulovic.monet.alg.{AccountAlg, AuthAlg}
import com.fradulovic.monet.auth.{Crypto, JwtExpire, Tokens}
import com.fradulovic.monet.conf.ConfigTypes.*
import com.fradulovic.monet.http.auth.AuthTypes.*
import com.fradulovic.monet.services.*
import com.fradulovic.monet.services.redis.RedisAuthService

import cats.effect.kernel.{Resource, Sync}
import cats.syntax.all.*
import dev.profunktor.auth.jwt.JwtAuth
import dev.profunktor.redis4cats.RedisCommands
import pdi.jwt.JwtAlgorithm
import skunk.Session

sealed abstract class Security[F[_]](val userAuth: AuthAlg[F], val jwtAuth: UserJwtAuth) {}

object Security {
  def make[F[_]: Sync](
      users: AccountAlg[F],
      session: Resource[F, Session[F]],
      redis: RedisCommands[F, String, String],
      userJwtConfig: JwtSecretKeyConfig,
      tokenKeyConfig: JwtAccessTokenKeyConfig,
      tokenExpiration: TokenExpiration,
      crypto: Crypto
  ): F[Security[F]] = {

    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(
        JwtAuth
          .hmac(
            userJwtConfig.value,
            JwtAlgorithm.HS256
          )
      )

    JwtExpire
      .make[F]
      .map(jwtExpire => Tokens.make[F](jwtExpire, tokenKeyConfig, tokenExpiration))
      .map(tokens =>
        new Security[F](
          RedisAuthService.make(
            users,
            redis,
            tokenExpiration,
            tokens,
            crypto
          ),
          userJwtAuth
        ) {}
      )
  }

}
