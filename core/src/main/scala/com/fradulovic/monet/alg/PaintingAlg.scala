package com.fradulovic.monet.alg

import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

import fs2.Stream

trait PaintingAlg[F[_]] {
  def getPaintings(searchPaintings: SearchPaintings): Stream[F, Painting]
  def getPaintingById(paintingId: PaintingId): F[Option[Painting]]
  def createPainting(createPainting: CreatePainting): F[PaintingId]
  def removePainting(paintingId: PaintingId): F[Unit]
  def updatePainting(updatePainting: UpdatePainting): F[Unit]
}
