package com.fradulovic.monet.interpreters

import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.http.clients.PaymentClient

import cats.effect.{IO, Ref}
import cats.syntax.all.*

object PaymentClientInterpreters {

  def successfulPaymentClient(paymentId: PaymentId): PaymentClient[IO] = new PaymentClient[IO] {
    def processPayment(payment: Payment): IO[PaymentId] = IO.pure(paymentId)
  }

  def failingPaymentClient(): PaymentClient[IO] = new PaymentClient[IO] {
    def processPayment(payment: Payment): IO[PaymentId] =
      PaymentError("failed client").raiseError[IO, PaymentId]
  }

  def recoveringPaymentClient(ref: Ref[IO, Int], paymentId: PaymentId): PaymentClient[IO] = new PaymentClient[IO] {
    def processPayment(payment: Payment): IO[PaymentId] =
      ref.get.flatMap { tries =>
        if tries === 2 then IO.pure(paymentId)
        else ref.update(_ + 1) >> PaymentError("failed client").raiseError[IO, PaymentId]
      }
  }
}
