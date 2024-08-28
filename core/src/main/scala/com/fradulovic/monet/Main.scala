package com.fradulovic.monet

import com.fradulovic.monet.auth.Crypto
import com.fradulovic.monet.conf.Config
import com.fradulovic.monet.modules.*
import com.fradulovic.monet.resources.*

import cats.effect.std.Supervisor
import cats.effect.{IO, IOApp}
import dev.profunktor.redis4cats.log4cats.*
import natchez.Trace.Implicits.noop
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    Slf4jLogger.create[IO].flatMap { implicit logger =>
      for {
        config <- Config.load[IO]
        crypto <- Crypto.make[IO](config.passwordSalt)
        _      <- Logger[IO].info("Loaded configuration")
        _ <- Supervisor[IO].use { implicit sp => // required for Background[F]
               MonetResources
                 .make[IO](config)
                 .evalMap { resources =>
                   val services = Services.make[IO](resources.postgres, resources.redis, config.cartExpiration)
                   Security
                     .make[IO](
                       services.accountService,
                       resources.postgres,
                       resources.redis,
                       config.jwtSecretKey,
                       config.jwtAccessTokenKey,
                       config.tokenExpiration,
                       crypto
                     )
                     .map { security =>
                       val clients = HttpClients.make[IO](config.paymentClientUri, resources.client)
                       val programs = Programs.make[IO](
                         services.cartService,
                         clients.paymentClient,
                         services.orderService,
                         config.checkout
                       )
                       val httpApi = HttpApi.make[IO](services, security, programs, crypto)
                       config.httpServer -> httpApi.httpApp
                     }
                 }
                 .flatMap { case (conf, httpApp) =>
                   MkHttpServer[IO].newEmber(conf, httpApp)
                 }
                 .useForever

             }
      } yield ()
    }
}
