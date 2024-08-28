package com.fradulovic.monet.domain

import scala.util.control.NoStackTrace

import com.fradulovic.monet.domain.NewTypes.*

import cats.Show
import cats.derived.*
import cats.kernel.Eq
import io.circe.Codec

case class Order(
    uuid: OrderId,
    paymentId: PaymentId,
    userId: UserId,
    paintings: Set[PaintingId],
    total: Price
) derives Show,
      Eq,
      Codec

sealed trait OrderOrPaymentError extends NoStackTrace {
  def cause: String
}
case class OrderError(cause: String)   extends OrderOrPaymentError
case class PaymentError(cause: String) extends OrderOrPaymentError
