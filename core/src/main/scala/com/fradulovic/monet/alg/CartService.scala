package com.fradulovic.monet.alg

import com.fradulovic.monet.domain.Cart
import com.fradulovic.monet.domain.NewTypes.*

trait CartService[F[_]] {
  def getCart(userId: UserId): F[Cart]
  def addPaintingToCart(paintingId: PaintingId, userId: UserId): F[Unit]
  def removePaintingFromCart(userId: UserId, paintingId: PaintingId): F[Unit]
  def deleteCart(userId: UserId): F[Unit]
}
