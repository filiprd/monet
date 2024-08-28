package com.fradulovic.monet.modules

import com.fradulovic.monet.alg.*
import com.fradulovic.monet.conf.ConfigTypes.CartExpiration
import com.fradulovic.monet.effects.GenUUID
import com.fradulovic.monet.services
import com.fradulovic.monet.services.*
import com.fradulovic.monet.services.postgres.*
import com.fradulovic.monet.services.redis.*

import cats.effect.kernel.{MonadCancelThrow, Resource}
import cats.effect.{Concurrent, Temporal}
import dev.profunktor.redis4cats.RedisCommands
import skunk.Session

sealed abstract class Services[F[_]](
    val accountService: AccountAlg[F],
    val paintingService: PaintingAlg[F],
    val cartService: CartService[F],
    val categoryService: CategoryAlg[F],
    val orderService: OrderAlg[F],
    val techniqueService: TechniqueAlg[F],
    val healthCheckService: HealthCheckAlg[F]
)

object Services {
  def make[F[_]: GenUUID: MonadCancelThrow: Concurrent: Temporal](
      session: Resource[F, Session[F]],
      redis: RedisCommands[F, String, String],
      expiration: CartExpiration
  ): Services[F] = {
    val _paintingService = PostgresPaintingService.make[F](session)
    new Services(
      PostgresAccountService.make[F](session),
      _paintingService,
      RedisCartService.make[F](redis, _paintingService, expiration),
      PostgresCategoryService.make[F](session),
      PostgresOrderService.make[F](session),
      PostgresTechniqueService.make[F](session),
      HealthCheck.make[F](session, redis)
    ) {}
  }
}
