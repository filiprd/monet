package com.fradulovic.monet.optics

import java.util.UUID

import com.fradulovic.monet.domain.HealthCheckStatus
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.gens.MonetGenerators.*

import monocle.law.discipline.*
import org.scalacheck.{Arbitrary, Cogen, Gen}
import weaver.FunSuite
import weaver.discipline.Discipline

object IsUUIDTest extends FunSuite with Discipline {

  given statusArb: Arbitrary[HealthCheckStatus] =
    Arbitrary(Gen.oneOf(HealthCheckStatus.Okay, HealthCheckStatus.Down))

  given uuidCogen: Cogen[UUID] =
    Cogen[(Long, Long)].contramap { uuid =>
      uuid.getLeastSignificantBits -> uuid.getMostSignificantBits
    }
  given paintingIdArb: Arbitrary[PaintingId] = Arbitrary(idGen[PaintingId])
  given paintingIdCogen: Cogen[PaintingId]   = Cogen[UUID].contramap[PaintingId](_.value)

  checkAll("Iso[Status._Bool]", IsoTests(HealthCheckStatus._Bool))
  checkAll("IsUUID[PaintingId]", IsoTests(IsUUID[PaintingId]._UUID))
}
