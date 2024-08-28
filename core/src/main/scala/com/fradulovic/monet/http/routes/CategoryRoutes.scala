package com.fradulovic.monet.http.routes

import com.fradulovic.monet.alg.CategoryAlg

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class CategoryRoutes[F[_]: Monad](categoryService: CategoryAlg[F]) extends Http4sDsl[F] {

  private val prefixPath = "/categories"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok(categoryService.getAll())
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
