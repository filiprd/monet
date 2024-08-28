package com.fradulovic.monet.services.redis

import com.fradulovic.monet.alg.{AccountAlg, AuthAlg}
import com.fradulovic.monet.auth.{Crypto, Tokens}
import com.fradulovic.monet.conf.ConfigTypes.TokenExpiration
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.domain.OrphanInstances.given

import cats.MonadThrow
import cats.syntax.all.*
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import io.circe.parser.decode
import io.circe.syntax.*
import pdi.jwt.JwtClaim

object RedisAuthService {

  def make[F[_]: MonadThrow](
      userService: AccountAlg[F],
      redis: RedisCommands[F, String, String],
      tokenExpiration: TokenExpiration,
      tokens: Tokens[F],
      crypto: Crypto
  ): AuthAlg[F] = new AuthAlg[F] {

    override def login(login: LoginUser): F[JwtToken] =
      userService.retrieveAccount(login.email).flatMap {
        case None => UserNotFound.raiseError[F, JwtToken]
        case Some(user) =>
          if user.password === crypto.encrypt(login.password) then {
            redis.get(login.email.value).flatMap {
              case Some(t) => JwtToken(t).pure[F]
              case None =>
                for {
                  t <- tokens.create
                  _ <- redis.setEx(t.value, user.asJson.noSpaces, tokenExpiration.value)
                         >> redis.setEx(login.email.value, t.value, tokenExpiration.value)
                } yield t
            }
          } else MonadThrow[F].raiseError[JwtToken](InvalidPassword)
      }

    override def logout(logout: LogoutUser): F[Unit] =
      redis.del(logout.token.show) >> redis.del(logout.email.value).void

    override def findLoggedUser(token: JwtToken)(claim: JwtClaim): F[Option[AuthUser]] =
      redis.get(token.value).map {
        _.flatMap(user => decode[User](user).toOption.map(AuthUser.fromUser))
      }
  }
}
