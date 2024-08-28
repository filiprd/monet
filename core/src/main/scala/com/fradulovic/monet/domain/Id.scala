package com.fradulovic.monet.domain

import com.fradulovic.monet.effects.GenUUID
import com.fradulovic.monet.optics.IsUUID

import cats.Functor
import cats.syntax.functor.*

object ID {

  /** Allows to create ids by using e.g., ID.make[F, UserId] */
  def make[F[_]: Functor: GenUUID, A: IsUUID]: F[A] =
    GenUUID[F].make.map(IsUUID[A]._UUID.get)

  /** Allows to reade ids by using e.g., ID.read[F, UserId](...) */
  def read[F[_]: Functor: GenUUID, A: IsUUID](str: String): F[A] =
    GenUUID[F].read(str).map(IsUUID[A]._UUID.get)
}
