package com.fradulovic.monet.http.routes

import com.fradulovic.monet.alg.HealthCheckAlg

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class HealthRoutes[F[_]: Monad](healthCheckService: HealthCheckAlg[F]) extends Http4sDsl[F] {
  private val prefixPath = "/health"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok(healthCheckService.status)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
