package com.fradulovic.monet.http.routes.secured

import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.http.req.AddPaintingToCartReq
import com.fradulovic.monet.interpreters.CartServiceInterpreter

import cats.effect.IO
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.implicits.*
import org.http4s.{Method, Request, Status}
import org.scalacheck.Gen

object CartRoutesTest extends HttpTestSuite {

  test("cart retrieval") {
    val gen: Gen[(AuthUser, Cart)] = for {
      au <- authUserGen
      c  <- cartGen
    } yield (au, c)

    forall(gen) { case (au, c) =>
      val cartService = CartServiceInterpreter.successfulCartService(c)
      val req         = Request[IO](Method.GET, uri"/cart")
      val routes      = CartRoutes[IO](cartService).routes(testAuthMiddleware(au))
      expectStatusAndBody(req, routes)(Status.Ok, c)
    }
  }

  test("add painting to cart") {
    val gen: Gen[(AuthUser, Cart, PaintingId)] = for {
      au  <- authUserGen
      c   <- cartGen
      pid <- idGen[PaintingId]
    } yield (au, c, pid)

    forall(gen) { case (au, c, pid) =>
      val cartService = CartServiceInterpreter.successfulCartService(c)
      val req         = Request[IO](Method.POST, uri"/cart").withEntity(AddPaintingToCartReq(pid))
      val routes      = CartRoutes[IO](cartService).routes(testAuthMiddleware(au))
      expectStatus(req, routes)(Status.Created)
    }
  }
}
