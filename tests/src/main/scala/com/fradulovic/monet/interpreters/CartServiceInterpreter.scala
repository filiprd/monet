package com.fradulovic.monet.interpreters

import com.fradulovic.monet.alg.CartService
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

import cats.effect.IO

object CartServiceInterpreter {

  def successfulCartService(cart: Cart): CartService[IO] = new CartService[IO] {
    def getCart(userId: UserId): IO[Cart] = IO.pure(cart)

    def addPaintingToCart(paintingId: PaintingId, userId: UserId): IO[Unit] = IO.unit

    def removePaintingFromCart(userId: UserId, paintingId: PaintingId): IO[Unit] = ???
    def deleteCart(userId: UserId): IO[Unit]                                     = ???
  }
}
