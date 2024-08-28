package com.fradulovic.monet.domain

import java.net.URI

import com.fradulovic.monet.domain.NewTypes.*

import cats.Show
import cats.syntax.show.*
import io.circe.Codec
import io.circe.Encoder.encodeURI

case class Painting(
    uuid: PaintingId,
    name: PaintingName,
    description: PaintingDescription,
    category: Category,
    technique: Technique,
    images: List[URI],
    price: Price,
    userId: UserId
) derives Codec

object Painting {
  given paintingShow: Show[Painting] =
    Show.show(p => s"Painting ${p.name.show} (${p.uuid.show}) with cost ${p.price.show}")
}

case class CreatePainting(
    name: PaintingName,
    description: PaintingDescription,
    categoryId: CategoryId,
    techniqueId: TechniqueId,
    images: List[URI],
    price: Price,
    userId: UserId
)

case class UpdatePainting(
    uuid: PaintingId,
    name: PaintingName,
    description: PaintingDescription,
    price: Price
)

sealed abstract case class SearchPaintings private (
    categoryId: Option[CategoryId] = None,
    techniqueId: Option[TechniqueId] = None,
    priceRange: Option[PriceRange] = None,
    userId: Option[UserId] = None
) {

  def withCategoryId(categoryId: CategoryId): SearchPaintings =
    new SearchPaintings(Some(categoryId), techniqueId, priceRange, userId) {}

  def withTechniqueId(techniqueId: TechniqueId): SearchPaintings =
    new SearchPaintings(categoryId, Some(techniqueId), priceRange, userId) {}

  def withPriceRange(priceRange: PriceRange): SearchPaintings =
    new SearchPaintings(categoryId, techniqueId, Some(priceRange), userId) {}

  def withUserId(userId: UserId): SearchPaintings =
    new SearchPaintings(categoryId, techniqueId, priceRange, Some(userId)) {}
}

object SearchPaintings {
  val all: SearchPaintings = new SearchPaintings() {}

  def withCategoryId(categoryId: CategoryId): SearchPaintings =
    new SearchPaintings(categoryId = Some(categoryId)) {}

  def withTechniqueId(techniqueId: TechniqueId): SearchPaintings =
    new SearchPaintings(techniqueId = Some(techniqueId)) {}

  def withPriceRange(priceRange: PriceRange): SearchPaintings =
    new SearchPaintings(priceRange = Some(priceRange)) {}

  def withUserId(userId: UserId): SearchPaintings =
    new SearchPaintings(userId = Some(userId)) {}
}
