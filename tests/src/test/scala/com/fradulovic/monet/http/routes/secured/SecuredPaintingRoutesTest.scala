package com.fradulovic.monet.http.routes.secured

import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.http.req.*
import com.fradulovic.monet.http.req.ParamTypes.*
import com.fradulovic.monet.interpreters.PaintingServiceInterpreters

import cats.effect.IO
import io.circe.syntax.*
import io.circe.{Encoder, JsonObject}
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.implicits.uri
import org.scalacheck.Gen

object SecuredPaintingRoutesTest extends HttpTestSuite {

  test("create painting") {
    val gen: Gen[(AuthUser, Painting)] = for {
      au <- authUserGen
      p  <- paintingGen
    } yield (au, p)

    forall(gen) { case (au, p) =>
      val paintingService = PaintingServiceInterpreters.successfulPaintingService(p.uuid)
      val reqPayload = CreatePaintingReq(
        PaintingNameParam(p.name.value).toOption.get,
        PaintingDescriptionParam(p.description.value).toOption.get,
        p.category.uuid,
        p.technique.uuid,
        p.images,
        PriceParam(p.price.value).toOption.get
      )
      val req      = Request[IO](Method.POST, uri"/paintings").withEntity(reqPayload)
      val routes   = SecuredPaintingRoutes(paintingService).routes(testAuthMiddleware(au))
      val expected = JsonObject.singleton("painting_id", p.uuid.asJson).asJson
      expectStatusAndBody(req, routes)(Status.Created, expected)
    }
  }

  test("update painting") {
    val gen: Gen[(AuthUser, PaintingId, PaintingName, PaintingDescription, Price)] = for {
      au  <- authUserGen
      pid <- idGen[PaintingId]
      n   <- strGen[PaintingName]
      d   <- strGen[PaintingDescription]
      p   <- priceGen
    } yield (au, pid, n, d, p)

    forall(gen) { case (au, pid, n, d, p) =>
      val paintingService = PaintingServiceInterpreters.successfulPaintingService(pid)
      val reqPayload = UpdatePaintingReq(
        PaintingId(pid.value),
        PaintingNameParam(n.value).toOption.get,
        PaintingDescriptionParam(d.value).toOption.get,
        PriceParam(p.value).toOption.get
      )
      val req    = Request[IO](Method.PUT, Uri.unsafeFromString(s"/paintings")).withEntity(reqPayload)
      val routes = SecuredPaintingRoutes(paintingService).routes(testAuthMiddleware(au))
      expectStatus(req, routes)(Status.Ok)
    }
  }

  test("delete painting") {
    val gen: Gen[(AuthUser, PaintingId)] = for {
      au  <- authUserGen
      pid <- idGen[PaintingId]
    } yield (au, pid)

    forall(gen) { case (au, pid) =>
      val paintingService = PaintingServiceInterpreters.successfulPaintingService(pid)
      val req             = Request[IO](Method.DELETE, Uri.unsafeFromString(s"/paintings/${pid.value.toString}"))
      val routes          = SecuredPaintingRoutes(paintingService).routes(testAuthMiddleware(au))
      expectStatus(req, routes)(Status.Ok)
    }
  }

}
