package com.fradulovic.monet.http.req

import scala.util.control.NoStackTrace

import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.domain.SearchPaintings

import io.circe.Codec

case class SearchPaintingsReq(
    category: Option[CategoryId],
    technique: Option[TechniqueId],
    priceRange: Option[PriceRangeParam],
    userId: Option[UserId]
) derives Codec {
  def toDomain: SearchPaintings = {
    val wCid = category.map(SearchPaintings.withCategoryId).getOrElse(SearchPaintings.all)
    val wTid = technique.map(wCid.withTechniqueId).getOrElse(wCid)
    val wPr  = priceRange.map(prp => wTid.withPriceRange(prp.toDomain)).getOrElse(wTid)
    userId.map(wPr.withUserId).getOrElse(wPr)
  }
}

case object NoParamInSearchPaintingsRequest extends NoStackTrace
