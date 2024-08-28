package com.fradulovic.monet.http.routes.secured

import com.fradulovic.monet.alg.AuthAlg
import com.fradulovic.monet.domain.{AuthUser, LogoutUser}

import cats.Monad
import cats.syntax.all.*
import dev.profunktor.auth.AuthHeaders
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}

final case class LogoutRoutes[F[_]: Monad](authService: AuthAlg[F]) extends Http4sDsl[F] {

  private val prefixPath = "/auth"

  private val httpRoutes: AuthedRoutes[AuthUser, F] = AuthedRoutes.of { case aReq @ POST -> Root / "logout" as user =>
    AuthHeaders
      .getBearerToken(aReq.req)
      .traverse_(t => authService.logout(LogoutUser(t, user.email))) >> NoContent()

  }

  def routes(authMiddleware: AuthMiddleware[F, AuthUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
