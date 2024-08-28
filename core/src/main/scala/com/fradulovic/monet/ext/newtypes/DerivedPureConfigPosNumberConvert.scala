package com.fradulovic.monet.ext.newtypes

import cats.implicits.catsSyntaxEither
import com.typesafe.config.ConfigValueType
import monix.newtypes.HasBuilder
import pureconfig.ConfigReader
import pureconfig.error.{UserValidationFailed, WrongType}

trait DerivedPureConfigPosNumberConvert {

  /** Provides ConfigReader[A] instance with positive number validation */
  given positiveNumberConfigReader[A](using builder: HasBuilder.Aux[A, Int]): ConfigReader[A] =
    ConfigReader.fromString(
      _.toIntOption.toRight(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER))).flatMap {
        case v if v > 0 => builder.build(v).bimap(bf => UserValidationFailed(bf.toReadableString), identity)
        case v          => Left(UserValidationFailed("Found non-positive number when reading configuration"))
      }
    )
}
