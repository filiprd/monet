package com.fradulovic.monet.http.routes

import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.http.req.*
import com.fradulovic.monet.http.req.ParamTypes.*
import com.fradulovic.monet.interpreters.PaintingServiceInterpreters

import cats.effect.IO
import cats.syntax.all.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.implicits.*
import org.scalacheck.Gen

object PaintingRoutesTest extends HttpTestSuite {

  test("get all paintings") {
    forall(Gen.nonEmptyListOf(paintingGen)) { paintings =>
      val paintingsService = PaintingServiceInterpreters.successfulPaintingService(paintings)
      val req              = Request[IO](Method.GET, uri"/paintings/all")
      val routes           = PaintingRoutes[IO](paintingsService).routes
      expectStatusAndStream(req, routes)(Status.Ok, paintings)
    }
  }

  test("search paintings by all params") {
    forall(Gen.nonEmptyListOf(paintingGen)) { paintings =>
      val paintingsService = PaintingServiceInterpreters.successfulPaintingService(paintings)
      val cid              = paintings.head.category.uuid
      val tid              = paintings.head.technique.uuid
      val p                = paintings.head.price
      val pr               = PriceRangeParam(LowerPriceInRangeParam.unsafe(p.value - 1), UpperPriceInRangeParam.unsafe(p.value + 1))
      val uid              = paintings.head.userId
      val reqPayload       = SearchPaintingsReq(Some(cid), Some(tid), Some(pr), Some(uid))
      val req              = Request[IO](Method.POST, uri"/paintings/search").withEntity(reqPayload)
      val routes           = PaintingRoutes[IO](paintingsService).routes
      val expected =
        PaintingServiceInterpreters.filterPaintings(paintings)(Some(cid), Some(tid), Some(pr.toDomain), Some(uid))
      expectStatusAndStream(req, routes)(Status.Ok, expected)
    }
  }

  test("search paintings by one param") {
    forall(Gen.nonEmptyListOf(paintingGen)) { paintings =>
      val paintingsService = PaintingServiceInterpreters.successfulPaintingService(paintings)
      val p                = paintings.head.price
      val pr               = PriceRangeParam(LowerPriceInRangeParam.unsafe(p.value - 1), UpperPriceInRangeParam.unsafe(p.value + 1))
      val reqPayload       = SearchPaintingsReq(None, None, Some(pr), None)
      val req              = Request[IO](Method.POST, uri"/paintings/search").withEntity(reqPayload)
      val routes           = PaintingRoutes[IO](paintingsService).routes
      val expected         = PaintingServiceInterpreters.filterPaintings(paintings)(None, None, Some(pr.toDomain), None)
      expectStatusAndStream(req, routes)(Status.Ok, expected)
    }
  }

  test("bad paintings search req") {
    forall(Gen.nonEmptyListOf(paintingGen)) { paintings =>
      val paintingsService = PaintingServiceInterpreters.successfulPaintingService(paintings)
      val reqPayload       = SearchPaintingsReq(None, None, None, None)
      val req              = Request[IO](Method.POST, uri"/paintings/search").withEntity(reqPayload)
      val routes           = PaintingRoutes[IO](paintingsService).routes
      expectStatus(req, routes)(Status.BadRequest)
    }
  }

  test("misspelled body param req") {
    forall(Gen.nonEmptyListOf(paintingGen)) { paintings =>
      import io.circe.literal.*
      val paintingsService = PaintingServiceInterpreters.successfulPaintingService(paintings)
      val req = Request[IO](Method.POST, uri"/paintings/search").withEntity(
        json"""{"userrId" : "d222dce2-454a-4aad-bae6-da67d720e1c7"}"""
      )
      val routes = PaintingRoutes[IO](paintingsService).routes
      expectStatus(req, routes)(Status.BadRequest)
    }
  }

  test("failing paintings search request") {
    forall(Gen.nonEmptyListOf(paintingGen)) { paintings =>
      val paintingsService = PaintingServiceInterpreters.failingPaintingService()
      val reqPayload       = SearchPaintingsReq(Some(paintings.head.category.uuid), None, None, None)
      val req              = Request[IO](Method.POST, uri"/paintings/search").withEntity(reqPayload)
      val routes           = PaintingRoutes[IO](paintingsService).routes
      expectFailedStream(req, routes)
    }
  }

  test("get painting by id") {
    forall(Gen.nonEmptyListOf(paintingGen)) { paintings =>
      val paintingsService = PaintingServiceInterpreters.successfulPaintingService(paintings)
      val anId             = paintings.head.uuid
      val uri              = Uri.fromString(s"/paintings/${anId.value.toString}").toOption.get
      val req              = Request[IO](Method.GET, uri)
      val routes           = PaintingRoutes[IO](paintingsService).routes
      val expectedPainting = paintings.find(_.uuid === anId).get
      expectStatusAndBody(req, routes)(Status.Ok, expectedPainting)
    }
  }
}
