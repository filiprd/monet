package com.fradulovic.monet.alg

import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.domain.Order

import cats.data.NonEmptySet
import fs2.Stream

trait OrderAlg[F[_]] {
  def getAll(userId: UserId): Stream[F, Order]
  def getOrderById(userId: UserId, orderId: OrderId): F[Option[Order]]
  def storeOrder(
      userId: UserId,
      paintings: NonEmptySet[PaintingId],
      total: Price,
      paymentId: PaymentId
  ): F[OrderId]
}
