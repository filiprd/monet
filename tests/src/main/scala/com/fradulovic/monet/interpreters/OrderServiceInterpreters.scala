package com.fradulovic.monet.interpreters

import com.fradulovic.monet.alg.OrderAlg
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

import cats.data.NonEmptySet
import cats.effect.IO
import fs2.Stream

object OrderServiceInterpreters {

  def successfulOrderService(orders: List[Order]): OrderAlg[IO] = new OrderAlg[IO] {
    override def storeOrder(
        userId: UserId,
        paintings: NonEmptySet[PaintingId],
        total: Price,
        paymentId: PaymentId
    ): IO[OrderId] = IO.pure(orders.head.uuid)

    override def getAll(userId: UserId): Stream[IO, Order] = Stream.emits(orders)
    override def getOrderById(userId: UserId, orderId: OrderId): IO[Option[Order]] =
      IO.pure(orders.find(_.uuid == orderId))
  }

  def failingOrderService(): OrderAlg[IO] = new OrderAlg[IO] {
    override def storeOrder(
        userId: UserId,
        paintings: NonEmptySet[PaintingId],
        total: Price,
        paymentId: PaymentId
    ): IO[OrderId] = IO.raiseError(OrderError("order error"))

    override def getAll(userId: UserId): Stream[IO, Order] = ???
    override def getOrderById(userId: UserId, orderId: OrderId): IO[Option[Order]] =
      IO.raiseError(OrderError("order retrieval error"))
  }

  def noOrdersService(): OrderAlg[IO] = new OrderAlg[IO] {
    override def storeOrder(
        userId: UserId,
        paintings: NonEmptySet[PaintingId],
        total: Price,
        paymentId: PaymentId
    ): IO[OrderId] = ???
    override def getAll(userId: UserId): Stream[IO, Order]                         = ???
    override def getOrderById(userId: UserId, orderId: OrderId): IO[Option[Order]] = IO.pure(None)
  }
}
