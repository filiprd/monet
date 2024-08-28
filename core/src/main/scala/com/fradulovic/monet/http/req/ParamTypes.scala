package com.fradulovic.monet.http.req

import com.fradulovic.monet.domain.NewTypes.{LowerPriceInRange, Password, UpperPriceInRange}
import com.fradulovic.monet.domain.PriceRange
import com.fradulovic.monet.ext.newtypes.DerivedCatsInstances
import com.fradulovic.monet.http.req.ParamTypes.{LowerPriceInRangeParam, UpperPriceInRangeParam}

import io.circe.Codec
import io.circe.generic.semiauto.*
import monix.newtypes.*
import monix.newtypes.integrations.DerivedCirceCodec

/** Defines types used in http request modelling, useful for validation of http
  * req body parameters
  *
  * Note: Parameters related to UUID types (e.g., PaintingId) are not defined
  * separately for http req since they would be identical to their definition in
  * the domain model
  */
object ParamTypes {

  type PaintingNameParam = PaintingNameParam.Type
  object PaintingNameParam extends NewtypeValidated[String] with DerivedCirceCodec {
    def apply(v: String): Either[BuildFailure[Type], Type] =
      if v.nonEmpty then Right(unsafeCoerce(v)) else Left(BuildFailure("Value must be non-empty"))
  }

  type PaintingDescriptionParam = PaintingDescriptionParam.Type
  object PaintingDescriptionParam extends NewtypeValidated[String] with DerivedCirceCodec {
    def apply(v: String): Either[BuildFailure[Type], Type] =
      if v.nonEmpty then Right(unsafeCoerce(v)) else Left(BuildFailure("Value must be non-empty"))
  }

  type PriceParam = PriceParam.Type
  object PriceParam extends NewtypeValidated[Int] with DerivedCirceCodec {
    def apply(v: Int): Either[BuildFailure[Type], Type] =
      if v > 0 then Right(unsafeCoerce(v)) else Left(BuildFailure("Value must be positive"))
  }

  type NameParam = NameParam.Type
  object NameParam extends NewtypeValidated[String] with DerivedCirceCodec with DerivedCatsInstances {
    def apply(v: String): Either[BuildFailure[Type], Type] =
      if v.nonEmpty then Right(unsafeCoerce(v)) else Left(BuildFailure("Value for name must be non-empty"))
  }

  type EmailParam = EmailParam.Type
  object EmailParam extends NewtypeValidated[String] with DerivedCirceCodec with DerivedCatsInstances {
    def apply(v: String): Either[BuildFailure[Type], Type] =
      if v.contains("@") && v.contains(".") then Right(unsafeCoerce(v))
      else Left(BuildFailure("Value for email is not in correct format"))
  }

  type PasswordParam = PasswordParam.Type
  object PasswordParam extends NewtypeValidated[String] with DerivedCirceCodec with DerivedCatsInstances {
    def apply(v: String): Either[BuildFailure[Type], Type] =
      if v.length > 8 then Right(unsafeCoerce(v))
      else Left(BuildFailure("Value for password must have at lest 8 characters"))
    extension (v: PasswordParam) def toDomain: Password = Password(v.value)
  }

  type LowerPriceInRangeParam = LowerPriceInRangeParam.Type
  object LowerPriceInRangeParam extends NewtypeValidated[Int] with DerivedCirceCodec {
    def apply(v: Int): Either[BuildFailure[Type], Type] =
      if v >= 0 then Right(unsafeCoerce(v))
      else Left(BuildFailure("Value for lower price in price range must be greater or equal to 0"))
  }

  type UpperPriceInRangeParam = UpperPriceInRangeParam.Type
  object UpperPriceInRangeParam extends NewtypeValidated[Int] with DerivedCirceCodec {
    def apply(v: Int): Either[BuildFailure[Type], Type] =
      if v > 0 then Right(unsafeCoerce(v))
      else Left(BuildFailure("Value for upper price in price range must be greater or equal to 0"))
  }
}

case class PriceRangeParam(lowerPrice: LowerPriceInRangeParam, upperPrice: UpperPriceInRangeParam) derives Codec {
  def toDomain: PriceRange = PriceRange(LowerPriceInRange(lowerPrice.value), UpperPriceInRange(upperPrice.value))
}
