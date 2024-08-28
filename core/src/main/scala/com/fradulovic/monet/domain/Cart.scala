package com.fradulovic.monet.domain

import scala.util.control.NoStackTrace

import com.fradulovic.monet.domain.NewTypes.*

import cats.Show
import cats.derived.*
import io.circe.Encoder

case class Cart(
    userId: UserId,
    paintings: Set[Painting],
    total: Price
) derives Show,
      Encoder

case object EmptyCartError extends NoStackTrace
