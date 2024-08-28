package com.fradulovic.monet.resources

import com.fradulovic.monet.conf.HttpServerConfig

import cats.effect.kernel.{Async, Resource}
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.typelevel.log4cats.Logger

trait MkHttpServer[F[_]] {
  def newEmber(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server]
}

object MkHttpServer {
  def apply[F[_]: MkHttpServer]: MkHttpServer[F] = summon[MkHttpServer[F]]

  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}")

  given forAsyncLogger[F[_]: Async: Logger]: MkHttpServer[F] =
    (config: HttpServerConfig, httpApp: HttpApp[F]) =>
      EmberServerBuilder
        .default[F]
        .withHost(config.host.value)
        .withPort(config.port.value)
        .withHttpApp(httpApp)
        .build
        .evalTap(showEmberBanner[F])
}
