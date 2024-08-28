package com.fradulovic.monet.ext.newtypes

import cats.implicits.catsSyntaxEither
import monix.newtypes.HasBuilder
import pureconfig.ConfigReader
import pureconfig.error.UserValidationFailed

trait DerivedPureConfigNonEmptyStringConvert {

  /** One way of providing ConfigReader[A] instance for
    * NewtypeWrapped[Underlying] is by simply implementing it manually and
    * handle invalid values when reading plain strings from conf
    *
    * Provides ConfigReader[A] instance with non-empty string validation
    */
  given nonEmptyStringConfigReader[A](using builder: HasBuilder.Aux[A, String]): ConfigReader[A] =
    ConfigReader.fromString(s =>
      if s.isEmpty || s.isBlank then Left(UserValidationFailed("Found empty string when reading configuration"))
      else builder.build(s).bimap(bf => UserValidationFailed(bf.toReadableString), identity)
    )

}
