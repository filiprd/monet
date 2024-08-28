package com.fradulovic.monet.domain

import com.fradulovic.monet.domain.NewTypes.*

import cats.Show
import cats.derived.*
import io.circe.Codec

case class Category(
    uuid: CategoryId,
    label: CategoryLabel
) derives Show,
      Codec
