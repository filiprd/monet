package com.fradulovic.monet.domain

import com.fradulovic.monet.domain.NewTypes.*

import cats.Show
import io.circe.Codec

case class CreditCard(
    name: CreditCardName,
    number: CreditCardNumber,
    expiration: CreditCardExpDate,
    cvc: CVC
) derives Codec

object CreditCard {
  given creditCardShow: Show[CreditCard] = Show.show(cc => s"${cc.name}") // used in tests generators
}
