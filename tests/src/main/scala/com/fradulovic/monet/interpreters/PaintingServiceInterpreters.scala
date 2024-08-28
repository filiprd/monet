package com.fradulovic.monet.interpreters

import scala.util.control.NoStackTrace

import com.fradulovic.monet.alg.PaintingAlg
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.syntax.all.*
import fs2.Stream

object PaintingServiceInterpreters {

  case object PaintingServiceError extends NoStackTrace

  def filterPaintings(
      paintings: List[Painting]
  )(cid: Option[CategoryId], tid: Option[TechniqueId], pr: Option[PriceRange], uid: Option[UserId]): List[Painting] =
    paintings
      .filter(p => cid.forall(cid => p.category.uuid === cid))
      .filter(p => tid.forall(tid => p.technique.uuid === tid))
      .filter(p => pr.forall(pr => p.price.value >= pr.lowerPrice.value && p.price.value <= pr.upperPrice.value))
      .filter(p => uid.forall(uid => p.userId === uid))

  def successfulPaintingService(paintings: List[Painting]): PaintingAlg[IO] = new PaintingAlg[IO] {
    def getPaintings(searchPaintings: SearchPaintings): Stream[IO, Painting] =
      Stream.emits(
        filterPaintings(paintings)(
          searchPaintings.categoryId,
          searchPaintings.techniqueId,
          searchPaintings.priceRange,
          searchPaintings.userId
        )
      )

    def getPaintingById(paintingId: PaintingId): IO[Option[Painting]]  = paintings.find(_.uuid === paintingId).pure[IO]
    def createPainting(createPainting: CreatePainting): IO[PaintingId] = ???
    def removePainting(paintingId: PaintingId): IO[Unit]               = ???
    def updatePainting(updatePainting: UpdatePainting): IO[Unit]       = ???
  }

  def successfulPaintingService(createdPaintingId: PaintingId): PaintingAlg[IO] = new PaintingAlg[IO] {
    def getPaintings(searchPaintings: SearchPaintings): Stream[IO, Painting] = ???
    def getPaintingById(paintingId: PaintingId): IO[Option[Painting]]        = ???
    def createPainting(createPainting: CreatePainting): IO[PaintingId]       = IO.pure(createdPaintingId)
    def removePainting(paintingId: PaintingId): IO[Unit]                     = IO.unit
    def updatePainting(updatePainting: UpdatePainting): IO[Unit]             = IO.unit
  }

  def failingPaintingService(): PaintingAlg[IO] = new PaintingAlg[IO] {
    def getPaintings(searchPaintings: SearchPaintings): Stream[IO, Painting] =
      Stream.raiseError[IO](PaintingServiceError)
    def getPaintingById(paintingId: PaintingId): IO[Option[Painting]]  = ???
    def createPainting(createPainting: CreatePainting): IO[PaintingId] = ???
    def removePainting(paintingId: PaintingId): IO[Unit]               = ???
    def updatePainting(updatePainting: UpdatePainting): IO[Unit]       = ???
  }

  def concurrentPaintingService(ref: Ref[IO, Map[PaintingId, Painting]]): PaintingAlg[IO] = new PaintingAlg[IO] {
    def getPaintings(searchPaintings: SearchPaintings): Stream[IO, Painting] =
      Stream.evalSeq(
        ref.get.map(m =>
          filterPaintings(m.values.toList)(
            searchPaintings.categoryId,
            searchPaintings.techniqueId,
            searchPaintings.priceRange,
            searchPaintings.userId
          )
        )
      )
    def getPaintingById(paintingId: PaintingId): IO[Option[Painting]]  = ref.get.map(_.get(paintingId))
    def createPainting(createPainting: CreatePainting): IO[PaintingId] = ???
    def removePainting(paintingId: PaintingId): IO[Unit]               = ref.update(_.removed(paintingId))
    def updatePainting(updatePainting: UpdatePainting): IO[Unit] = ref.update { m =>
      m.get(updatePainting.uuid)
        .fold(m)(p =>
          m.updated(
            updatePainting.uuid,
            p.copy(name = updatePainting.name, description = updatePainting.description, price = updatePainting.price)
          )
        )
    }
  }

}
