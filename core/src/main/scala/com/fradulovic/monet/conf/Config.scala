package com.fradulovic.monet.conf

import com.fradulovic.monet.conf.ConfigTypes.*

import cats.MonadThrow
import cats.effect.kernel.Sync
import cats.effect.std.Env
import cats.implicits.*
import pureconfig.generic.derivation.default.*
import pureconfig.module.catseffect.syntax.*
import pureconfig.module.ip4s.given
import pureconfig.{ConfigReader, ConfigSource}

sealed trait DataStoreConfig derives ConfigReader
case class PostgresConfig(host: Host, port: Port, db: Database, user: DbUser, pass: DbPassword, max: MaxConnections)
    extends DataStoreConfig

sealed trait InMemoryDb derives ConfigReader
case class RedisConfig(uri: RedisURI) extends InMemoryDb

case class CheckoutConfig(
    retriesLimit: RetriesLimit,
    retriesBackoff: RetriesBackoff,
    orderCreationBackoff: OrderCreationBackoff
) derives ConfigReader

case class HttpClientConfig(timeout: Timeout, idleTimeInPool: IdleTimeInPool) derives ConfigReader

case class HttpServerConfig(host: Ip4sHost, port: Ip4sPort) derives ConfigReader

case class Config(
    jwtSecretKey: JwtSecretKeyConfig,
    jwtAccessTokenKey: JwtAccessTokenKeyConfig,
    passwordSalt: PasswordSalt,
    tokenExpiration: TokenExpiration,
    cartExpiration: CartExpiration,
    checkout: CheckoutConfig,
    paymentClientUri: PaymentClientUri,
    httpClient: HttpClientConfig,
    postgres: PostgresConfig,
    redis: RedisConfig,
    httpServer: HttpServerConfig
) derives ConfigReader

object Config {
  def load[F[_]: Sync: Env]: F[Config] =
    for {
      env       <- Env[F].get("MONET_ENV").map(_.flatMap(MonetEnvironment.fromString))
      configFile = ConfigSource.file("application.conf")
      maybeConfig <- env.map {
                       case MonetEnvironment.Prod => configFile.at("prod").loadF[F, Config]()
                       case MonetEnvironment.Test => configFile.at("test").loadF[F, Config]()
                     }.sequence
      res <- MonadThrow[F].fromOption(
               maybeConfig,
               new Throwable("Monet not started: No $MONET_ENV env variable found: it must be set to 'prod' or 'test'")
             )
    } yield res
}
