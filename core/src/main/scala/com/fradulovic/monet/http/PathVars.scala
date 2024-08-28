package com.fradulovic.monet.http

import java.util.UUID

import com.fradulovic.monet.domain.NewTypes.*

import cats.implicits.*

/** Defines http request path variables */
object PathVars {

  protected class UUIDVar[A](f: UUID => A) {
    def unapply(uuid: String): Option[A] = Either.catchNonFatal(f(UUID.fromString(uuid))).toOption
  }

  object PaintingIdVar extends UUIDVar(PaintingId.apply)
  object UserIdVar     extends UUIDVar(UserId.apply)
  object OrderIdVar    extends UUIDVar(OrderId.apply)
}
