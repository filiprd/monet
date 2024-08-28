package com.fradulovic.monet.http.routes.secured

import com.fradulovic.monet.domain.*
import com.fradulovic.monet.ext.http4s.Refined.*
import com.fradulovic.monet.programs.CheckoutProgram

import cats.MonadThrow
import cats.syntax.all.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}

final case class CheckoutRoutes[F[_]: JsonDecoder: MonadThrow](checkoutProgram: CheckoutProgram[F])
    extends Http4sDsl[F] {

  private val prefixPath = "/checkout"

  private val httpRoutes: AuthedRoutes[AuthUser, F] = AuthedRoutes.of { case aReq @ POST -> Root as user =>
    aReq.req.decodeR[CreditCard] { creditCard =>
      checkoutProgram
        .checkout(user.id, creditCard)
        .flatMap(Ok(_))
        .recoverWith {
          case EmptyCartError   => BadRequest("Shopping cart must not be empty!")
          case pe: PaymentError => BadRequest(pe.cause)
          case oe: OrderError   => BadRequest(oe.cause)
        }
    }

  }

  def routes(authMiddleware: AuthMiddleware[F, AuthUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
