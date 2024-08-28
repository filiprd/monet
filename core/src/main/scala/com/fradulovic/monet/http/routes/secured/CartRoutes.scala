package com.fradulovic.monet.http.routes.secured

import com.fradulovic.monet.alg.CartService
import com.fradulovic.monet.domain.AuthUser
import com.fradulovic.monet.ext.http4s.Refined.*
import com.fradulovic.monet.http.PathVars.PaintingIdVar
import com.fradulovic.monet.http.req.AddPaintingToCartReq

import cats.MonadThrow
import cats.syntax.all.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}

final case class CartRoutes[F[_]: JsonDecoder: MonadThrow](cartService: CartService[F]) extends Http4sDsl[F] {

  private val prefixPath = "/cart"

  private val httpRoutes: AuthedRoutes[AuthUser, F] = AuthedRoutes.of {

    case GET -> Root as user =>
      Ok(cartService.getCart(user.id))

    case aReq @ POST -> Root as user =>
      aReq.req.decodeR[AddPaintingToCartReq] { addPaintingToCartReq =>
        cartService.addPaintingToCart(addPaintingToCartReq.toDomain, user.id) >> Created()
      }

    case DELETE -> Root / PaintingIdVar(paintingId) as user =>
      Ok(cartService.removePaintingFromCart(user.id, paintingId))
  }

  def routes(authMiddleware: AuthMiddleware[F, AuthUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
