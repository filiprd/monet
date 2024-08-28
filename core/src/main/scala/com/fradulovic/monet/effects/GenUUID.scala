package com.fradulovic.monet.effects

import java.util.UUID

import cats.ApplicativeThrow
import cats.effect.Sync

trait GenUUID[F[_]] {
  def make: F[UUID]
  def read(s: String): F[UUID]
}

object GenUUID {
  def apply[F[_]: GenUUID]: GenUUID[F] = summon

  given genUUIDInstance[F[_]: Sync]: GenUUID[F] = new GenUUID[F] {
    override def make            = Sync[F].blocking(UUID.randomUUID())
    override def read(s: String) = ApplicativeThrow[F].catchNonFatal(UUID.fromString(s))
  }
}
