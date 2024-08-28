package com.fradulovic.monet.resources

import com.fradulovic.monet.conf.*

import cats.effect.Concurrent
import cats.effect.kernel.{Resource, Temporal}
import cats.effect.std.Console
import cats.syntax.all.*
import dev.profunktor.redis4cats.effect.MkRedis
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import fs2.io.net.Network
import natchez.Trace
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import skunk.*
import skunk.codec.text.*
import skunk.implicits.*

class MonetResources[F[_]](
    val client: Client[F],
    val postgres: Resource[F, Session[F]],
    val redis: RedisCommands[F, String, String]
) {}

object MonetResources {
  def make[F[_]: Concurrent: Temporal: Network: Logger: Trace: Console: MkRedis: MkHttpClient](
      config: Config
  ): Resource[F, MonetResources[F]] = {

    def checkPostgresConnection(
        postgres: Resource[F, Session[F]]
    ): F[Unit] =
      postgres.use { session =>
        session.unique(sql"SELECT version();".query(text)).flatMap { v =>
          Logger[F].info(s"Connected to Postgres $v")
        }
      }

    def checkRedisConnection(
        redis: RedisCommands[F, String, String]
    ): F[Unit] =
      redis.info.flatMap {
        _.get("redis_version").traverse_ { v =>
          Logger[F].info(s"Connected to Redis $v")
        }
      }

    def mkPostgresResource(config: PostgresConfig): SessionPool[F] =
      Session
        .pooled[F](
          host = config.host.value,
          port = config.port.value,
          user = config.user.value,
          password = Some(config.pass.value),
          database = config.db.value,
          max = config.max.value
        )
        .evalTap(checkPostgresConnection)

    def mkRedisResource(config: RedisConfig): Resource[F, RedisCommands[F, String, String]] =
      Redis[F].utf8(config.uri.value.toString).evalTap(checkRedisConnection)

    (
      MkHttpClient[F].newEmber(config.httpClient),
      mkPostgresResource(config.postgres),
      mkRedisResource(config.redis)
    ).parMapN(new MonetResources[F](_, _, _) {})
  }
}
