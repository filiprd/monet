package com.fradulovic.monet.http.req

import com.fradulovic.monet.domain.NewTypes.*

import io.circe.Codec

case class AddPaintingToCartReq(paintingId: PaintingId) derives Codec {
  def toDomain: PaintingId = PaintingId(paintingId.value)
}
