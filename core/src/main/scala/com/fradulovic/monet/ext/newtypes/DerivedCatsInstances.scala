package com.fradulovic.monet.ext.newtypes

import cats.*
import cats.syntax.all.*
import monix.newtypes.{HasExtractor, *}

trait DerivedCatsInstances {

  given showInstance[A, U](using extractor: HasExtractor.Aux[A, U], show: Show[U]): Show[A] = new Show[A] {
    override def show(t: A): String = extractor.extract(t).show
  }

  given eqInstance[A, U](using extractor: HasExtractor.Aux[A, U], eq: Eq[U]): Eq[A] = new Eq[A] {
    override def eqv(x: A, y: A): Boolean = extractor.extract(x).eqv(extractor.extract(y))
  }
}
