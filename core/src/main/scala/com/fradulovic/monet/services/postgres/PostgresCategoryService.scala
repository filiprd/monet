package com.fradulovic.monet.services.postgres

import com.fradulovic.monet.alg.CategoryAlg
import com.fradulovic.monet.domain.Category

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import skunk.*
import skunk.implicits.*

import SharedCodecs.*

object PostgresCategoryService {
  def make[F[_]: MonadCancelThrow](postgres: Resource[F, Session[F]]): CategoryAlg[F] = new CategoryAlg[F] {
    import CategorySQL.*
    override def getAll(): F[List[Category]] =
      postgres.use(_.execute(selectAll)) // // assumes all categories can fit into memory
  }
}

private object CategorySQL {
  val selectAll: Query[Void, Category] = sql"select * from categories".query(category)
}
