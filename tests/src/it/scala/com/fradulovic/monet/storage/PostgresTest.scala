package com.fradulovic.monet.storage

import cats.data.*
import cats.effect.*
import cats.implicits.*
import com.fradulovic.monet.db.ResourceSuite
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.services.postgres.*
import com.fradulovic.monet.services.postgres.SharedCodecs.*
import natchez.Trace.Implicits.noop
import org.scalacheck.Gen
import skunk.*
import skunk.implicits.*

import scala.collection.immutable.SortedSet

object PostgresTest extends ResourceSuite {

  val flushTables: List[Command[Void]] =
    List("orders", "paintings", "users", "categories", "techniques").map { table =>
      sql"DELETE FROM #$table".command
    }

  type Res = Resource[IO, Session[IO]]

  override def sharedResource: Resource[IO, Res] =
    Session
      .pooled[IO](
        host = "localhost",
        port = 5432,
        user = "postgres",
        password = Some("changeme"),
        database = "monet_test",
        max = 10
      )
      .beforeAll {
        _.use { s =>
          flushTables.traverse_(s.execute)
        }
      }

  val insertCategory: Command[CategoryId ~ CategoryLabel] =
    sql"INSERT INTO categories VALUES ($categoryId, $categoryLabel)".command.contramap { case id ~ l => (id, l) }

  test("categories") { postgres =>
    val cs = PostgresCategoryService.make[IO](postgres)
    forall(categoryGen) { case Category(id, label) =>
      for {
        x <- cs.getAll()
        _ <- postgres.use(_.prepareR(insertCategory).use(_.execute(id ~ label)))
        y <- cs.getAll()
      } yield expect.all(
        x.isEmpty,
        y.count(_.uuid === id) === 1
      )
    }
  }

  val insertTechnique: Command[TechniqueId ~ TechniqueLabel] =
    sql"INSERT INTO techniques VALUES ($techniqueId, $techniqueLabel)".command.contramap { case id ~ l => (id, l) }

  test("techniques") { postgres =>
    val ts = PostgresTechniqueService.make[IO](postgres)
    forall(techniqueGen) { case Technique(id, label) =>
      for {
        x <- ts.getAll()
        _ <- postgres.use(_.prepareR(insertTechnique).use(_.execute(id ~ label)))
        y <- ts.getAll()
      } yield expect.all(
        x.isEmpty,
        y.count(_.uuid === id) === 1
      )
    }
  }

  test("accounts") { postgres =>
    val as = PostgresAccountService.make[IO](postgres)
    forall(userGen) { user =>
      for {
        x   <- as.retrieveAccount(user.email)
        _   <- as.createAccount(CreateAccount(user.email, user.password, user.name))
        y   <- as.retrieveAccount(user.email)
        err <- as.createAccount(CreateAccount(user.email, user.password, user.name)).attempt
      } yield expect.all(x.isEmpty, y.get.email === user.email, err.isLeft)
    }
  }

  test("paintings") { postgres =>
    val gen: Gen[(Painting, User)] = for {
      a <- paintingGen
      u <- userGen
    } yield (a, u)

    val ps   = PostgresPaintingService.make[IO](postgres)
    val accs = PostgresAccountService.make[IO](postgres)

    forall(gen) { (painting, user) =>
      def createPainting(userId: UserId): CreatePainting =
        CreatePainting(
          painting.name,
          painting.description,
          painting.category.uuid,
          painting.technique.uuid,
          painting.images,
          painting.price,
          userId
        )

      for {
        uid <- accs.createAccount(CreateAccount(user.email, user.password, user.name))
        _   <- postgres.use(_.prepareR(insertCategory).use(_.execute(painting.category.uuid ~ painting.category.label)))
        _ <-
          postgres.use(_.prepareR(insertTechnique).use(_.execute(painting.technique.uuid ~ painting.technique.label)))
        x   <- ps.getPaintingById(painting.uuid)
        e   <- ps.getPaintings(SearchPaintings.all).compile.toList
        pid <- ps.createPainting(createPainting(uid))
        p   <- ps.getPaintings(SearchPaintings.all).compile.toList
        y   <- ps.getPaintingById(pid)
        z <- ps
               .getPaintings(SearchPaintings.withCategoryId(CategoryId(painting.category.uuid.value)))
               .compile
               .toList
        w <- ps
               .getPaintings(SearchPaintings.withTechniqueId(TechniqueId(painting.technique.uuid.value)))
               .compile
               .toList
        q <-
          ps
            .getPaintings(
              SearchPaintings.withPriceRange(
                PriceRange(LowerPriceInRange(painting.price.value - 10), UpperPriceInRange(painting.price.value + 10))
              )
            )
            .compile
            .toList
        _ <- ps.updatePainting(
               UpdatePainting(
                 pid,
                 PaintingName(painting.name.value.toUpperCase),
                 PaintingDescription(painting.description.value.toUpperCase),
                 Price(painting.price.value + 100)
               )
             )
        u <- ps.getPaintingById(pid)
        _ <- ps.removePainting(pid)
        s <- ps.getPaintingById(pid)
      } yield expect.all(
        x.isEmpty,
        e.isEmpty,
        y.count(_.name === painting.name) === 1,
        z.count(_.uuid === pid) === 1,
        z.size === 1,
        w.count(_.uuid === pid) === 1,
        q.count(_.uuid === pid) === 1,
        p.nonEmpty,
        u.nonEmpty,
        u.get.name.value === painting.name.value.toUpperCase,
        u.get.description.value === painting.description.value.toUpperCase,
        u.get.price === Price(painting.price.value + 100),
        s.isEmpty
      )
    }
  }

  test("orders") { postgres =>
    val gen = for {
      o <- orderGen
      u <- userGen
    } yield (o.copy(userId = u.uuid), u)

    val os = PostgresOrderService.make[IO](postgres)
    val as = PostgresAccountService.make[IO](postgres)

    forall(gen) { case (order, user) =>
      for {
        uid <- as.createAccount(CreateAccount(user.email, user.password, user.name))
        x   <- os.getAll(uid).compile.toList
        y   <- os.getOrderById(uid, order.uuid)
        oid <-
          os.storeOrder(uid, NonEmptySet.fromSetUnsafe(SortedSet.from(order.paintings)), order.total, order.paymentId)
        w <- os.getAll(uid).compile.toList
        z <- os.getOrderById(uid, oid)
        err <-
          os.storeOrder(uid, NonEmptySet.fromSetUnsafe(SortedSet.from(order.paintings)), order.total, order.paymentId)
            .attempt
      } yield expect.all(
        x.isEmpty,
        y.isEmpty,
        w.count(_.uuid === oid) === 1,
        z === Some(order.copy(uuid = oid, userId = uid)),
        w.headOption.map(_.uuid) === z.map(_.uuid),
        err.isLeft
      )
    }
  }
}
