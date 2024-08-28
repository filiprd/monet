package com.fradulovic.monet.http.req

import java.util.UUID

import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.domain.UpdatePainting
import com.fradulovic.monet.http.req.ParamTypes.*

import io.circe.Codec

case class UpdatePaintingReq(
    paintingId: PaintingId,
    name: PaintingNameParam,
    description: PaintingDescriptionParam,
    price: PriceParam
) derives Codec {
  def toDomain: UpdatePainting =
    UpdatePainting(
      PaintingId(paintingId.value),
      PaintingName(name.value),
      PaintingDescription(description.value),
      Price(price.value)
    )
}
