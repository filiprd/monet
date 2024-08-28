package com.fradulovic.monet.resources

import com.fradulovic.monet.conf.HttpClientConfig

import cats.effect.kernel.{Async, Resource}
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

trait MkHttpClient[F[_]] {
  def newEmber(config: HttpClientConfig): Resource[F, Client[F]]
}

object MkHttpClient {
  def apply[F[_]: MkHttpClient]: MkHttpClient[F] = summon[MkHttpClient[F]]

  given forAsync[F[_]: Async]: MkHttpClient[F] =
    (config: HttpClientConfig) =>
      EmberClientBuilder
        .default[F]
        .withTimeout(config.timeout.value)
        .withIdleTimeInPool(config.idleTimeInPool.value)
        .build
}
