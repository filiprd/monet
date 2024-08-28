package com.fradulovic.monet.ext.newtypes

import java.util.UUID

import com.fradulovic.monet.optics.IsUUID

import monix.newtypes.{HasBuilder, HasExtractor, NewtypeWrapped}
import monocle.Iso

/** Adding a self-type in order to make derivation of IsUUID possible only for
  * NewtypeWrapped[UUID] instances
  */
trait DerivedIsUUID { self: NewtypeWrapped[UUID] =>

  given newtypeIsUUIDInstance[A](using
      extractor: HasExtractor.Aux[A, UUID],
      builder: HasBuilder.Aux[A, UUID]
  ): IsUUID[A] = new IsUUID[A] {
    val _UUID = Iso[UUID, A](x => unsafeCoerce(x).asInstanceOf[A])(extractor.extract)
  }

}
