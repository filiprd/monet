package com.fradulovic.monet.http.routes

import com.fradulovic.monet.alg.AuthAlg
import com.fradulovic.monet.domain.OrphanInstances.given
import com.fradulovic.monet.domain.{InvalidPassword, LoginUser, UserNotFound}
import com.fradulovic.monet.ext.http4s.Refined.*

import cats.MonadThrow
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class LoginRoutes[F[_]: JsonDecoder: MonadThrow](authService: AuthAlg[F]) extends Http4sDsl[F] {

  private val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
    req.decodeR[LoginUser] { loginUser =>
      authService
        .login(loginUser)
        .flatMap(Ok(_))
        .recoverWith { case UserNotFound | InvalidPassword =>
          Forbidden() // for security reasons so that we don't leak information
        }
    }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
