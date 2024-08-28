package com.fradulovic.monet.http.clients

import com.fradulovic.monet.conf.ConfigTypes.PaymentClientUri
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.domain.{Payment, PaymentError}

import cats.effect.MonadCancelThrow
import cats.syntax.all.*
import org.http4s.*
import org.http4s.Method.POST
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.*
import org.http4s.client.dsl.Http4sClientDsl

trait PaymentClient[F[_]] {
  def processPayment(payment: Payment): F[PaymentId]
}

object PaymentClient {
  def make[F[_]: JsonDecoder: MonadCancelThrow](paymentUri: PaymentClientUri, client: Client[F]) = new PaymentClient[F]
    with Http4sClientDsl[F] {

    def processPayment(payment: Payment): F[PaymentId] =
      Uri.fromString(paymentUri.value.toString).liftTo[F].flatMap { uri =>
        client.run(POST(payment, uri)).use { resp =>
          resp.status match {
            case Status.Ok | Status.Conflict =>
              resp.asJsonDecode[PaymentId]
            case status =>
              PaymentError(Option(status.reason).getOrElse("unknown")).raiseError[F, PaymentId]
          }
        }
      }
  }
}
