package com.fradulovic.monet.services.redis

import com.fradulovic.monet.alg.{CartService, PaintingAlg}
import com.fradulovic.monet.conf.ConfigTypes.CartExpiration
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.effects.GenUUID
import com.fradulovic.monet.services.*

import cats.MonadThrow
import cats.syntax.all.*
import dev.profunktor.redis4cats.RedisCommands

object RedisCartService {

  def make[F[_]: GenUUID: MonadThrow](
      redis: RedisCommands[F, String, String],
      paintingService: PaintingAlg[F],
      expiration: CartExpiration
  ): CartService[F] = new CartService[F] {

    override def getCart(userId: UserId): F[Cart] =
      redis.sMembers(userId.show).flatMap {
        _.toList.traverseFilter { case uuid =>
          for {
            paintingId <- ID.read[F, PaintingId](uuid)
            painting   <- paintingService.getPaintingById(paintingId)
          } yield painting
        }.map { paintings =>
          Cart(userId, paintings.toSet, paintings.foldMap(_.price))
        }
      }

    override def addPaintingToCart(paintingId: PaintingId, userId: UserId): F[Unit] =
      redis.sAdd(userId.show, paintingId.show) >> redis.expire(userId.show, expiration.value).void

    override def removePaintingFromCart(userId: UserId, paintingId: PaintingId): F[Unit] =
      redis.sRem(userId.show, paintingId.show).void

    override def deleteCart(userId: UserId): F[Unit] =
      redis.del(userId.show).void
  }
}
