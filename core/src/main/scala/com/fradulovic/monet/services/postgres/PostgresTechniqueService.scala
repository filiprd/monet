package com.fradulovic.monet.services.postgres

import com.fradulovic.monet.alg.TechniqueAlg
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.services.*

import cats.effect.kernel.{MonadCancelThrow, Resource}
import skunk.*
import skunk.implicits.*

import SharedCodecs.*

object PostgresTechniqueService {
  def make[F[_]: MonadCancelThrow](postgres: Resource[F, Session[F]]): TechniqueAlg[F] = new TechniqueAlg[F] {
    import TechniqueSQL.*
    override def getAll(): F[List[Technique]] =
      postgres.use(_.execute(selectAll)) // assumes all techniques can fit into memory
  }
}

private object TechniqueSQL {
  val selectAll: Query[Void, Technique] = sql"select * from techniques".query(technique)
}
