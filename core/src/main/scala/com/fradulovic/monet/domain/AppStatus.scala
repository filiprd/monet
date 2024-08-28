package com.fradulovic.monet.domain

import com.fradulovic.monet.domain.NewTypes.{PostgresStatus, RedisStatus}

import cats.derived.*
import cats.kernel.Eq
import io.circe.Encoder
import monocle.Iso

case class AppStatus(postgresStatus: PostgresStatus, redisStatus: RedisStatus) derives Encoder

sealed trait HealthCheckStatus derives Eq
object HealthCheckStatus {
  case object Okay extends HealthCheckStatus
  case object Down extends HealthCheckStatus

  val _Bool: Iso[HealthCheckStatus, Boolean] =
    Iso[HealthCheckStatus, Boolean] {
      case Okay => true
      case Down => false
    }(if (_) Okay else Down)

  given statusEncoder: Encoder[HealthCheckStatus] = Encoder.forProduct1("status")(_.toString)
}
