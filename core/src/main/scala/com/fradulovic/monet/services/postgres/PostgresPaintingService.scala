package com.fradulovic.monet.services.postgres

import java.net.URI

import com.fradulovic.monet.alg.PaintingAlg
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.effects.GenUUID

import cats.effect.kernel.{Concurrent, Resource}
import cats.syntax.all.*
import fs2.Stream
import io.circe.syntax.*
import skunk.*
import skunk.circe.codec.json.jsonb
import skunk.codec.all.*
import skunk.implicits.*

import SharedCodecs.*

object PostgresPaintingService {

  def make[F[_]: GenUUID: Concurrent](postgres: Resource[F, Session[F]]): PaintingAlg[F] = new PaintingAlg[F] {
    import PaintingSQL.*

    override def getPaintings(searchPaintings: SearchPaintings): Stream[F, Painting] = {
      val af    = buildSearchPaintingsFragment(searchPaintings)
      val query = af.fragment.query(decoder)

      for {
        session <- Stream.resource(postgres)
        p       <- Stream.resource(session.prepareR(query))
        res     <- p.stream(af.argument, 128)
      } yield res
    }

    override def getPaintingById(paintingId: PaintingId): F[Option[Painting]] = {
      val af    = selectById(paintingId)
      val query = af.fragment.query(decoder)
      postgres.use(_.prepareR(query).use(_.option(af.argument)))
    }

    override def createPainting(createPainting: CreatePainting): F[PaintingId] =
      postgres.use(
        _.prepareR(insertPainting)
          .use(cmd =>
            ID.make[F, PaintingId].flatMap(paintingId => cmd.execute(paintingId ~ createPainting).as(paintingId))
          )
      )

    override def removePainting(paintingId: PaintingId): F[Unit] =
      postgres.use(_.prepareR(deletePainting).use(_.execute(paintingId).void))

    override def updatePainting(updatePainting: UpdatePainting): F[Unit] =
      postgres.use(_.prepareR(PaintingSQL.updatePainting).use(_.execute(updatePainting).void))
  }
}

private object PaintingSQL {

  val paintingName: Codec[PaintingName]               = varchar.imap(PaintingName(_))(_.value)
  val paintingDescription: Codec[PaintingDescription] = varchar.imap(PaintingDescription(_))(_.value)
  val lowerPrice: Codec[LowerPriceInRange]            = int4.imap(LowerPriceInRange(_))(_.value)
  val upperPricePrice: Codec[UpperPriceInRange]       = int4.imap(UpperPriceInRange(_))(_.value)
  val decoder: Decoder[Painting] =
    (paintingId ~ userId ~ paintingName ~ paintingDescription ~ categoryId ~ categoryLabel ~ techniqueId ~ techniqueLabel ~ jsonb[
      List[URI]
    ] ~ price).map { case id ~ idu ~ n ~ d ~ cid ~ cnm ~ tid ~ tnm ~ imgs ~ p =>
      Painting(id, n, d, Category(cid, cnm), Technique(tid, tnm), imgs, p, idu)
    }

  val selectAll: Fragment[Void] =
    sql"""
          SELECT p.id, p.user_id, p.name, p.description, c.id, c.label, t.id, t.label, p.images, p.price
          FROM paintings AS p
          INNER JOIN categories AS c ON p.category_id = c.id
          INNER JOIN techniques AS t ON p.technique_id = t.id
      """

  def buildSearchPaintingsFragment(searchPaintings: SearchPaintings): AppliedFragment = {

    val categoryF  = sql"p.category_id = $categoryId"
    val techniqueF = sql"p.technique_id = $techniqueId"
    val priceF     = sql"p.price BETWEEN $lowerPrice AND $upperPricePrice"
    val userF      = sql"p.user_id = $userId"

    val conds: List[AppliedFragment] =
      List(
        searchPaintings.categoryId.map(categoryF),
        searchPaintings.techniqueId.map(techniqueF),
        searchPaintings.priceRange.map(pr => priceF(pr.lowerPrice, pr.upperPrice)),
        searchPaintings.userId.map(userF)
      ).flatten

    val filter =
      if conds.isEmpty then AppliedFragment.empty
      else conds.foldSmash(void" WHERE ", void" AND ", AppliedFragment.empty)

    selectAll(Void) |+| filter
  }

  val selectById: PaintingId => AppliedFragment = (id: PaintingId) =>
    selectAll(Void) |+| sql" WHERE p.id = $paintingId" (id)

  val insertPainting: Command[PaintingId ~ CreatePainting] =
    sql"""
         INSERT INTO paintings
         VALUES ($paintingId, $paintingName, $paintingDescription, $categoryId, $techniqueId, $jsonb, $price, $userId)
       """.command.contramap { case pid ~ ca =>
      (pid, ca.name, ca.description, ca.categoryId, ca.techniqueId, ca.images.asJson, ca.price, ca.userId)
    }

  val deletePainting: Command[PaintingId] =
    sql"""
         DELETE FROM paintings
         WHERE id = $paintingId
       """.command

  val updatePainting: Command[UpdatePainting] =
    sql"""
         UPDATE paintings
         SET name = $paintingName, description = $paintingDescription, price = $price
         WHERE id = $paintingId
       """.command.contramap(up => (up.name, up.description, up.price, up.uuid))
}
