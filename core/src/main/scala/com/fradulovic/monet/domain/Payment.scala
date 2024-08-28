package com.fradulovic.monet.domain

import com.fradulovic.monet.domain.NewTypes.*

import cats.Show
import cats.syntax.all.*
import io.circe.Encoder

case class Payment(
    userId: UserId,
    total: Price,
    creditCard: CreditCard
) derives Encoder

object Payment {
  given paymentShow: Show[Payment] = Show.show(p => s"${p.userId.show} payed ${p.total.show}")
}
