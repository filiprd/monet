package com.fradulovic.monet.http.routes

import com.fradulovic.monet.alg.PaintingAlg
import com.fradulovic.monet.domain.SearchPaintings
import com.fradulovic.monet.ext.http4s.Refined.*
import com.fradulovic.monet.http.PathVars.PaintingIdVar
import com.fradulovic.monet.http.req.SearchPaintingsReq

import cats.MonadThrow
import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class PaintingRoutes[F[_]: MonadThrow: Concurrent](paintingsService: PaintingAlg[F]) extends Http4sDsl[F] {

  private def buildSearch(r: SearchPaintingsReq): Option[SearchPaintings] =
    if r.userId.isEmpty && r.category.isEmpty && r.technique.isEmpty && r.priceRange.isEmpty then None
    else Some(r.toDomain)

  private val prefixPath = "/paintings"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root / "all" => Ok(paintingsService.getPaintings(SearchPaintings.all))

    case req @ POST -> Root / "search" =>
      req.decodeR[SearchPaintingsReq] { searchPaintingsReq =>
        buildSearch(searchPaintingsReq)
          .map(r => Ok(paintingsService.getPaintings(r)))
          .getOrElse(BadRequest("At least one search param must be provided"))
      }

    case GET -> Root / PaintingIdVar(uuid) => Ok(paintingsService.getPaintingById(uuid))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
