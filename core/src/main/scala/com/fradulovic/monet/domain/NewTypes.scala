package com.fradulovic.monet.domain

import java.util.UUID

import com.fradulovic.monet.ext.newtypes.{DerivedCatsInstances, DerivedIsUUID}

import cats.*
import cats.implicits.*
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec

object NewTypes {

  type PaintingId = PaintingId.Type
  object PaintingId extends NewtypeWrapped[UUID] with DerivedCirceCodec with DerivedCatsInstances with DerivedIsUUID {
    given paintingIdOrdering: Ordering[PaintingId] = derive[Ordering]
  }

  type PaintingName = PaintingName.Type
  object PaintingName extends NewtypeWrapped[String] with DerivedCirceCodec with DerivedCatsInstances

  type PaintingDescription = PaintingDescription.Type
  object PaintingDescription extends NewtypeWrapped[String] with DerivedCirceCodec with DerivedCatsInstances

  type Price = Price.Type
  object Price extends NewtypeWrapped[Int] with DerivedCirceCodec with DerivedCatsInstances {
    given priceMonoid: Monoid[Price] = new Monoid[Price] {
      override def empty: Price                         = Price(0)
      override def combine(p1: Price, p2: Price): Price = Price(p1.value + p2.value)
    }
  }

  type LowerPriceInRange = LowerPriceInRange.Type
  object LowerPriceInRange extends NewtypeWrapped[Int] with DerivedCirceCodec

  type UpperPriceInRange = UpperPriceInRange.Type
  object UpperPriceInRange extends NewtypeWrapped[Int] with DerivedCirceCodec

  type CategoryId = CategoryId.Type
  object CategoryId extends NewtypeWrapped[UUID] with DerivedCirceCodec with DerivedCatsInstances with DerivedIsUUID

  type CategoryLabel = CategoryLabel.Type
  object CategoryLabel extends NewtypeWrapped[String] with DerivedCirceCodec with DerivedCatsInstances

  val cardNameRgx = "^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$".r
  type CreditCardName = CreditCardName.Type
  object CreditCardName extends NewtypeValidated[String] with DerivedCirceCodec {
    def apply(v: String): Either[BuildFailure[Type], Type] =
      if cardNameRgx.pattern.matcher(v).matches then Right(unsafeCoerce(v))
      else Left(BuildFailure("Value for card name is not in correct format"))
  }

  type CreditCardNumber = CreditCardNumber.Type
  object CreditCardNumber extends NewtypeValidated[Long] with DerivedCirceCodec {
    def apply(v: Long): Either[BuildFailure[Type], Type] =
      if v.toString.length == 16 then Right(unsafeCoerce(v))
      else Left(BuildFailure("Value for card number is not in correct format"))
  }

  type CreditCardExpDate = CreditCardExpDate.Type
  object CreditCardExpDate extends NewtypeValidated[Int] with DerivedCirceCodec {
    def apply(v: Int): Either[BuildFailure[Type], Type] =
      if v.toString.length == 4 then Right(unsafeCoerce(v))
      else Left(BuildFailure("Value for card exp date is not in correct format"))
  }

  type CVC = CVC.Type
  object CVC extends NewtypeValidated[Int] with DerivedCirceCodec {
    def apply(v: Int): Either[BuildFailure[Type], Type] =
      if v.toString.length == 3 then Right(unsafeCoerce(v)) // no American Express yet :)
      else Left(BuildFailure("Value for card cvc is not in correct format"))
  }

  type OrderId = OrderId.Type
  object OrderId extends NewtypeWrapped[UUID] with DerivedCirceCodec with DerivedCatsInstances with DerivedIsUUID

  type PaymentId = PaymentId.Type
  object PaymentId extends NewtypeWrapped[UUID] with DerivedCirceCodec with DerivedCatsInstances with DerivedIsUUID

  type TechniqueId = TechniqueId.Type
  object TechniqueId extends NewtypeWrapped[UUID] with DerivedCirceCodec with DerivedCatsInstances with DerivedIsUUID

  type TechniqueLabel = TechniqueLabel.Type
  object TechniqueLabel extends NewtypeWrapped[String] with DerivedCirceCodec with DerivedCatsInstances

  type UserId = UserId.Type
  object UserId extends NewtypeWrapped[UUID] with DerivedCirceCodec with DerivedCatsInstances with DerivedIsUUID

  type Name = Name.Type
  object Name extends NewtypeWrapped[String] with DerivedCirceCodec with DerivedCatsInstances

  type Email = Email.Type
  object Email extends NewtypeWrapped[String] with DerivedCirceCodec with DerivedCatsInstances

  type EncryptedPassword = EncryptedPassword.Type
  object EncryptedPassword extends NewtypeWrapped[String] with DerivedCirceCodec with DerivedCatsInstances

  type Password = Password.Type
  object Password extends NewtypeWrapped[String] with DerivedCirceCodec with DerivedCatsInstances

  type CartId = CartId.Type
  object CartId extends NewtypeWrapped[UUID] with DerivedCirceCodec with DerivedIsUUID

  type PostgresStatus = PostgresStatus.Type
  object PostgresStatus extends NewtypeWrapped[HealthCheckStatus] with DerivedCirceCodec

  type RedisStatus = RedisStatus.Type
  object RedisStatus extends NewtypeWrapped[HealthCheckStatus] with DerivedCirceCodec
}
