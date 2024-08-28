package com.fradulovic.monet.http.routes.secured

import com.fradulovic.monet.alg.OrderAlg
import com.fradulovic.monet.domain.AuthUser
import com.fradulovic.monet.http.PathVars.{OrderIdVar, UserIdVar}

import cats.Monad
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}

final case class OrderRoutes[F[_]: Monad](orderService: OrderAlg[F]) extends Http4sDsl[F] {

  private val prefixPath = "/orders"

  private val httpRoutes: AuthedRoutes[AuthUser, F] = AuthedRoutes.of {

    case GET -> Root / "all" / UserIdVar(userId) as _ =>
      Ok(orderService.getAll(userId))

    case GET -> Root / OrderIdVar(orderId) as user =>
      Ok(orderService.getOrderById(user.id, orderId))
  }

  def routes(authMiddleware: AuthMiddleware[F, AuthUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
