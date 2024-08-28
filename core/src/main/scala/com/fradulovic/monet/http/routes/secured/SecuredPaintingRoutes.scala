package com.fradulovic.monet.http.routes.secured

import com.fradulovic.monet.alg.PaintingAlg
import com.fradulovic.monet.domain.AuthUser
import com.fradulovic.monet.ext.http4s.Refined.*
import com.fradulovic.monet.http.PathVars.PaintingIdVar
import com.fradulovic.monet.http.req.*

import cats.MonadThrow
import cats.syntax.all.*
import io.circe.JsonObject
import io.circe.syntax.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}

final case class SecuredPaintingRoutes[F[_]: JsonDecoder: MonadThrow](paintingsService: PaintingAlg[F])
    extends Http4sDsl[F] {

  private val prefixPath = "/paintings"

  private val httpRoutes: AuthedRoutes[AuthUser, F] = AuthedRoutes.of {

    case aReq @ POST -> Root as user =>
      aReq.req.decodeR[CreatePaintingReq] { createPainting =>
        paintingsService.createPainting(createPainting.toDomain(user.id)).flatMap { paintingId =>
          Created(JsonObject.singleton("painting_id", paintingId.asJson))
        }
      }

    case aReq @ PUT -> Root as _ =>
      aReq.req.decodeR[UpdatePaintingReq] { updatePainting =>
        Ok(paintingsService.updatePainting(updatePainting.toDomain))
      }

    case DELETE -> Root / PaintingIdVar(paintingId) as _ =>
      Ok(paintingsService.removePainting(paintingId))

  }

  def routes(authMiddleware: AuthMiddleware[F, AuthUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
