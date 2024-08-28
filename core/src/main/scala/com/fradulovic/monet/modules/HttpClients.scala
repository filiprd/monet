package com.fradulovic.monet.modules

import com.fradulovic.monet.conf.ConfigTypes.PaymentClientUri
import com.fradulovic.monet.http.clients.PaymentClient

import cats.effect.MonadCancelThrow
import org.http4s.circe.JsonDecoder
import org.http4s.client.Client

sealed abstract class HttpClients[F[_]](val paymentClient: PaymentClient[F]) {}

object HttpClients {
  def make[F[_]: JsonDecoder: MonadCancelThrow](paymentUri: PaymentClientUri, client: Client[F]) =
    new HttpClients(PaymentClient.make[F](paymentUri, client)) {}
}
