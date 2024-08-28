package com.fradulovic.monet.services.postgres

import com.fradulovic.monet.alg.OrderAlg
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.effects.GenUUID
import com.fradulovic.monet.services.*

import cats.data.{NonEmptySet, NonEmptySetImpl}
import cats.effect.Concurrent
import cats.effect.kernel.Resource
import cats.syntax.all.*
import fs2.Stream
import skunk.*
import skunk.circe.codec.json.jsonb
import skunk.codec.all.*
import skunk.implicits.*

import SharedCodecs.*

object PostgresOrderService {

  def make[F[_]: Concurrent: GenUUID](postgres: Resource[F, Session[F]]): OrderAlg[F] = new OrderAlg[F] {
    import OrderSQL.*

    override def getAll(userId: UserId): Stream[F, Order] =
      for {
        session <- Stream.resource(postgres)
        p       <- Stream.resource(session.prepareR(selectByUserId))
        res     <- p.stream(userId, 128)
      } yield res

    override def getOrderById(userId: UserId, orderId: OrderId): F[Option[Order]] =
      postgres.use(_.prepareR(selectByUserIdAndOrderId).use(_.option(userId ~ orderId)))

    override def storeOrder(
        userId: UserId,
        paintings: NonEmptySet[PaintingId],
        total: Price,
        paymentId: PaymentId
    ): F[OrderId] =
      postgres.use(
        _.prepareR(insertOrder)
          .use(cmd =>
            ID.make[F, OrderId].flatMap { uuid =>
              val order = Order(uuid, paymentId, userId, paintings.toSortedSet, total)
              cmd.execute(order).as(uuid)
            }
          )
      )
  }
}

private object OrderSQL {
  val orderId: Codec[OrderId] = uuid.imap(OrderId(_))(_.value)
  val codec: Codec[Order] = (orderId ~ paymentId ~ userId ~ jsonb[Set[PaintingId]] ~ price).imap {
    case o ~ p ~ u ~ a ~ t => Order(o, p, u, a, t)
  } { case Order(o, p, u, a, t) => o ~ p ~ u ~ a ~ t }

  val selectByUserId: Query[UserId, Order] =
    sql"select * from orders where user_id = $userId".query(codec)

  val selectByUserIdAndOrderId: Query[UserId ~ OrderId, Order] =
    sql"select * from orders where user_id = $userId and id = $orderId".query(codec)

  val insertOrder: Command[Order] = sql"insert into orders values ($codec)".command
}
