package com.fradulovic.monet.http.routes

import com.fradulovic.monet.alg.TechniqueAlg

import cats.Monad
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class TechniqueRoutes[F[_]: Monad](techniqueService: TechniqueAlg[F]) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/techniques"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok(techniqueService.getAll())
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
