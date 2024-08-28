package com.fradulovic.monet.conf

import java.net.URI

import scala.concurrent.duration.FiniteDuration

import com.fradulovic.monet.conf.ConfigTypes.RetriesLimit.Type
import com.fradulovic.monet.ext.newtypes.{DerivedPureConfigNonEmptyStringConvert, DerivedPureConfigPosNumberConvert}

import com.comcast.ip4s
import com.typesafe.config.ConfigValueType
import monix.newtypes.*
import monix.newtypes.integrations.DerivedPureConfigConvert
import pureconfig.ConfigReader
import pureconfig.error.*

object ConfigTypes {

  type CartExpiration = CartExpiration.Type
  object CartExpiration extends NewtypeWrapped[FiniteDuration] with DerivedPureConfigConvert

  type TokenExpiration = TokenExpiration.Type
  object TokenExpiration extends NewtypeWrapped[FiniteDuration] with DerivedPureConfigConvert

  type PasswordSalt = PasswordSalt.Type
  object PasswordSalt extends NewtypeWrapped[String] with DerivedPureConfigNonEmptyStringConvert

  type RedisURI = RedisURI.Type
  object RedisURI extends NewtypeWrapped[URI] with DerivedPureConfigConvert

  type RetriesBackoff = RetriesBackoff.Type
  object RetriesBackoff extends NewtypeWrapped[FiniteDuration] with DerivedPureConfigConvert

  type OrderCreationBackoff = OrderCreationBackoff.Type
  object OrderCreationBackoff extends NewtypeWrapped[FiniteDuration] with DerivedPureConfigConvert

  type PaymentClientUri = PaymentClientUri.Type
  object PaymentClientUri extends NewtypeWrapped[URI] with DerivedPureConfigConvert

  type Timeout = Timeout.Type
  object Timeout extends NewtypeWrapped[FiniteDuration] with DerivedPureConfigConvert

  type IdleTimeInPool = IdleTimeInPool.Type
  object IdleTimeInPool extends NewtypeWrapped[FiniteDuration] with DerivedPureConfigConvert

  /** Another way of providing ConfigReader[A] instance is by deriving it with
    * DerivedPureConfigConvert. It is a convenient option if there is no
    * validation. However if validation of values is needed and we still want to
    * use DerivedPureConfigConvert, we need to extend
    * NewtypeValidated[Underlying] and implement an apply method for validation
    */
  type Host = Host.Type
  object Host extends NewtypeValidated[String] with DerivedPureConfigConvert {
    def apply(v: String): Either[BuildFailure[Type], Type] =
      if (v.isEmpty || v.isBlank)
        Left(BuildFailure("Empty postgres host"))
      else
        Right(unsafeCoerce(v))
  }

  type Port = Port.Type
  object Port extends NewtypeWrapped[Int] {
    given configReader: ConfigReader[Port] =
      ConfigReader.fromString(
        _.toIntOption.toRight(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER))).flatMap {
          case v if v >= 1024 && v <= 65535 => Right(Port(v))
          case v                            => Left(UserValidationFailed("Port number not valid"))
        }
      )
  }

  type Database = Database.Type
  object Database extends NewtypeWrapped[String] with DerivedPureConfigNonEmptyStringConvert

  type DbUser = DbUser.Type
  object DbUser extends NewtypeWrapped[String] with DerivedPureConfigNonEmptyStringConvert

  type DbPassword = DbPassword.Type
  object DbPassword extends NewtypeWrapped[String] with DerivedPureConfigNonEmptyStringConvert

  type MaxConnections = MaxConnections.Type
  object MaxConnections extends NewtypeWrapped[Int] with DerivedPureConfigPosNumberConvert

  type JwtAccessTokenKeyConfig = JwtAccessTokenKeyConfig.Type
  object JwtAccessTokenKeyConfig extends NewtypeWrapped[String] with DerivedPureConfigNonEmptyStringConvert

  type JwtSecretKeyConfig = JwtSecretKeyConfig.Type
  object JwtSecretKeyConfig extends NewtypeWrapped[String] with DerivedPureConfigNonEmptyStringConvert

  type RetriesLimit = RetriesLimit.Type
  object RetriesLimit extends NewtypeWrapped[Int] with DerivedPureConfigPosNumberConvert

  type Ip4sHost = Ip4sHost.Type
  object Ip4sHost extends NewtypeWrapped[ip4s.Host] with DerivedPureConfigConvert

  type Ip4sPort = Ip4sPort.Type
  object Ip4sPort extends NewtypeWrapped[ip4s.Port] with DerivedPureConfigConvert
}
