package com.fradulovic.monet.http.req

import java.net.URI

import com.fradulovic.monet.domain.CreatePainting
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.http.req.ParamTypes.*

import io.circe.Codec

case class CreatePaintingReq(
    name: PaintingNameParam,
    description: PaintingDescriptionParam,
    categoryId: CategoryId,
    techniqueId: TechniqueId,
    images: List[URI],
    price: PriceParam
) derives Codec {
  def toDomain(userId: UserId): CreatePainting =
    CreatePainting(
      PaintingName(name.value),
      PaintingDescription(description.value),
      categoryId,
      techniqueId,
      images,
      Price(price.value),
      userId
    )
}
