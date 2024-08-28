package com.fradulovic.monet.optics

import java.util.UUID

import monix.newtypes.HasExtractor
import monocle.Iso

/** Used for creating ids, i.e., newtype types whose underlying type is UUID. It
  * will allow calling Id.make[F, A] only for those types A which are
  * NewtypeWrapped[UUID]
  */
trait IsUUID[A] {
  def _UUID: Iso[UUID, A]
}

object IsUUID {
  def apply[A: IsUUID]: IsUUID[A] = summon

  /** Useful for deriving IsUUID instances of NewtypeWrapped[UUID] types. We
    * need function f which is an apply function of defined newtype, because the
    * .build method of HasBuilder.Aux[A, UUID] returns Either and therefore
    * can't be used in Iso[UUID,A]. Actually, we could have used it and matched
    * it on only Right(v) for NewtypeWrapped[UUID] since it would always return
    * Right values, but it would not be safe to do so for NewtypeValidated[UUID]
    */
  def newtypeIsUUIDInstance[A](f: UUID => A)(using extractor: HasExtractor.Aux[A, UUID]): IsUUID[A] = new IsUUID[A] {
    val _UUID = Iso[UUID, A](f)(extractor.extract)
  }
}
