package com.fradulovic.monet.services

import scala.concurrent.duration.*

import com.fradulovic.monet.alg.HealthCheckAlg
import com.fradulovic.monet.domain.NewTypes.{PostgresStatus, RedisStatus}
import com.fradulovic.monet.domain.{AppStatus, HealthCheckStatus}

import cats.effect.Temporal
import cats.effect.implicits.*
import cats.effect.kernel.Resource
import cats.syntax.all.*
import dev.profunktor.redis4cats.RedisCommands
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

object HealthCheck {
  def make[F[_]: Temporal](
      postgres: Resource[F, Session[F]],
      redis: RedisCommands[F, String, String]
  ): HealthCheckAlg[F] =
    new HealthCheckAlg[F] {
      override def status: F[AppStatus] = {

        val q: Query[Void, Int] = sql"SELECT pid FROM pg_stat_activity".query(int4)

        val redisHealth: F[RedisStatus] =
          redis.ping
            .map(_.nonEmpty)
            .timeout(1.second)
            .map(HealthCheckStatus._Bool.reverseGet)
            .orElse(HealthCheckStatus.Down.pure[F].widen)
            .map(RedisStatus.apply)

        val postgresHealth: F[PostgresStatus] =
          postgres
            .use(_.execute(q))
            .map(_.nonEmpty)
            .timeout(1.second)
            .map(HealthCheckStatus._Bool.reverseGet)
            .orElse(HealthCheckStatus.Down.pure[F].widen)
            .map(PostgresStatus.apply)

        (postgresHealth, redisHealth).parMapN(AppStatus.apply)
      }
    }
}
