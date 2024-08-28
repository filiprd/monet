package com.fradulovic.monet.storage

import scala.concurrent.duration.*
import com.fradulovic.monet.db.ResourceSuite
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.gens.MonetGenerators.*
import cats.effect.*
import cats.effect.kernel.Ref
import cats.implicits.*
import com.fradulovic.monet.auth.*
import com.fradulovic.monet.conf.ConfigTypes.*
import com.fradulovic.monet.http.auth.AuthTypes.UserJwtAuth
import com.fradulovic.monet.interpreters.*
import com.fradulovic.monet.services.redis.*
import dev.profunktor.auth.jwt.*
import dev.profunktor.redis4cats.log4cats.*
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import org.scalacheck.Gen
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import pdi.jwt.*

object RedisTest extends ResourceSuite {

  given lg: SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  type Res = RedisCommands[IO, String, String]

  override def sharedResource: Resource[IO, Res] =
    Redis[IO]
      .utf8("redis://localhost")
      .beforeAll(_.flushAll)

  val exp         = CartExpiration(30.seconds)
  val tokenConfig = JwtAccessTokenKeyConfig("monet")
  val tokenExp    = TokenExpiration(30.seconds)
  val jwtClaim    = JwtClaim("test")
  val userJwtAuth = UserJwtAuth(JwtAuth.hmac("monet", JwtAlgorithm.HS256))

  test("shopping cart") { redis =>
    val gen: Gen[(UserId, Painting, Painting)] = for {
      uid <- idGen[UserId]
      a1  <- paintingGen
      a2  <- paintingGen
    } yield (uid, a1, a2)

    forall(gen) { case (uid, a1, a2) =>
      Ref.of[IO, Map[PaintingId, Painting]](Map(a1.uuid -> a1, a2.uuid -> a2)).flatMap { ref =>
        val ps = PaintingServiceInterpreters.concurrentPaintingService(ref)
        val cs = RedisCartService.make[IO](redis, ps, exp)
        for {
          x <- cs.getCart(uid)
          _ <- cs.addPaintingToCart(a1.uuid, uid)
          _ <- cs.addPaintingToCart(a2.uuid, uid)
          y <- cs.getCart(uid)
          _ <- cs.removePaintingFromCart(uid, a2.uuid)
          z <- cs.getCart(uid)
          _ <- cs.deleteCart(uid)
          w <- cs.getCart(uid)
        } yield expect.all(
          x.paintings.isEmpty,
          y.paintings.size === 2,
          z.paintings.size === 1,
          w.paintings.isEmpty
        )
      }
    }
  }

  test("auth") { redis =>
    val gen: Gen[(User, Password)] = for {
      u <- userGen
      p <- strGen[Password]
    } yield (u, p)

    forall(gen) { case (user, password) =>
      for {
        tokens           <- JwtExpire.make[IO].map(Tokens.make[IO](_, tokenConfig, tokenExp))
        crypto           <- Crypto.make[IO](PasswordSalt("testPass"))
        encryptedPassword = crypto.encrypt(password)
        accountService    = AccountServiceInterpreter.successfulAccountService(user.copy(password = encryptedPassword))
        authService       = RedisAuthService.make[IO](accountService, redis, tokenExp, tokens, crypto)
        x                <- authService.findLoggedUser(JwtToken("invalid"))(jwtClaim)
        y                <- authService.login(LoginUser(user.email, password))
        d                <- jwtDecode[IO](y, userJwtAuth.value).attempt
        q <-
          authService.login(LoginUser(Email("unexisting@test.com"), Password("whatever"))).attempt // non-existing user
        w <- authService.login(LoginUser(user.email, Password("wrong"))).attempt // wrong password
        r <- authService.findLoggedUser(y)(jwtClaim)
        m <- redis.get(y.value)
        _ <- authService.logout(LogoutUser(y, user.email))
        n <- redis.get(y.value)
      } yield expect.all(
        x.isEmpty,
        d.isRight,
        q == Left(UserNotFound),
        w == Left(InvalidPassword),
        r.fold(false)(_.email === user.email),
        m.nonEmpty,
        n.isEmpty
      )
    }
  }

}
