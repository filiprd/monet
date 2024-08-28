package com.fradulovic.monet.domain

import com.fradulovic.monet.domain.NewTypes.Price
import com.fradulovic.monet.gens.MonetGenerators.*

import cats.kernel.laws.discipline.MonoidTests
import org.scalacheck.Arbitrary
import weaver.FunSuite
import weaver.discipline.Discipline

object NewTypesTest extends FunSuite with Discipline {

  given arbPrice: Arbitrary[Price] = Arbitrary(priceGen)

  checkAll("Monoid[Price]", MonoidTests[Price].monoid)
}
