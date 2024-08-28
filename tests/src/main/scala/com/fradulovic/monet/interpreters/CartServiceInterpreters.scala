package com.fradulovic.monet.interpreters

import java.util.UUID

import scala.util.control.NoStackTrace

import com.fradulovic.monet.alg.CartService
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

import cats.effect.IO

object CartServiceInterpreters {

  def successfulCartService(cart: Cart): CartService[IO] = new CartService[IO] {
    override def getCart(userId: UserId): IO[Cart]                                        = IO.pure(cart)
    override def deleteCart(userId: UserId): IO[Unit]                                     = IO.unit
    override def addPaintingToCart(paintingId: PaintingId, userId: UserId): IO[Unit]      = ???
    override def removePaintingFromCart(userId: UserId, paintingId: PaintingId): IO[Unit] = ???
  }

  def emptyCartService(): CartService[IO] = new CartService[IO] {
    val emptyCart                                                                         = Cart(UserId(UUID.fromString("6c35f4c5-2beb-4b32-81e5-cf540dc66252")), Set(), Price(0))
    override def getCart(userId: UserId): IO[Cart]                                        = IO.pure(emptyCart)
    override def deleteCart(userId: UserId): IO[Unit]                                     = ???
    override def addPaintingToCart(paintingId: PaintingId, userId: UserId): IO[Unit]      = ???
    override def removePaintingFromCart(userId: UserId, paintingId: PaintingId): IO[Unit] = ???
  }

  def failingCartDeletionService(cart: Cart): CartService[IO] = new CartService[IO] {
    override def getCart(userId: UserId): IO[Cart]                                        = IO.pure(cart)
    override def deleteCart(userId: UserId): IO[Unit]                                     = IO.raiseError(new NoStackTrace {})
    override def addPaintingToCart(paintingId: PaintingId, userId: UserId): IO[Unit]      = ???
    override def removePaintingFromCart(userId: UserId, paintingId: PaintingId): IO[Unit] = ???
  }

}
