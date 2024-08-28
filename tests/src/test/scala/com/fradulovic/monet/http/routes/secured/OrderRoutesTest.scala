package com.fradulovic.monet.http.routes.secured

import java.util.UUID

import com.fradulovic.monet.alg.OrderAlg
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.domain.{AuthUser, Order}
import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.interpreters.OrderServiceInterpreters

import cats.effect.IO
import org.http4s.{Method, Request, *}
import org.scalacheck.Gen

object OrderRoutesTest extends HttpTestSuite {

  test("successful orders retrieval") {
    val gen: Gen[(AuthUser, List[Order])] = for {
      au   <- authUserGen
      ords <- Gen.nonEmptyListOf(orderGen)
    } yield (au, ords)

    forall(gen) { case (au, orders) =>
      val successfulOrderService: OrderAlg[IO] = OrderServiceInterpreters.successfulOrderService(orders)
      val req                                  = Request[IO](Method.GET, Uri.unsafeFromString(s"/orders/all/${UUID.randomUUID().toString}"))
      val routes                               = OrderRoutes[IO](successfulOrderService).routes(testAuthMiddleware(au))
      expectStatusAndStream(req, routes)(Status.Ok, orders)
    }
  }

  test("successful one order retrieval") {
    val gen: Gen[(AuthUser, List[Order])] = for {
      au   <- authUserGen
      ords <- Gen.nonEmptyListOf(orderGen)
    } yield (au, ords)

    forall(gen) { case (au, orders) =>
      val successfulOrderService: OrderAlg[IO] = OrderServiceInterpreters.successfulOrderService(orders)
      val headOrder                            = orders.headOption
      val req                                  = Request[IO](Method.GET, Uri.unsafeFromString(s"/orders/${headOrder.get.uuid.value.toString}"))
      val routes                               = OrderRoutes[IO](successfulOrderService).routes(testAuthMiddleware(au))
      expectStatusAndBody(req, routes)(Status.Ok, headOrder)
    }
  }

  test("no order to retrieve") {
    val gen: Gen[(AuthUser, OrderId)] = for {
      au  <- authUserGen
      oid <- idGen[OrderId]
    } yield (au, oid)

    forall(gen) { case (au, oid) =>
      val zeroOrderService: OrderAlg[IO] = OrderServiceInterpreters.noOrdersService()
      val req                            = Request[IO](Method.GET, Uri.unsafeFromString(s"/orders/${UUID.randomUUID().toString}"))
      val routes                         = OrderRoutes[IO](zeroOrderService).routes(testAuthMiddleware(au))
      expectStatusAndBody(req, routes)(Status.Ok, None)
    }
  }

  test("failing order retrieval") {
    forall(authUserGen) { au =>
      val failingOrderService: OrderAlg[IO] = OrderServiceInterpreters.failingOrderService()
      val req                               = Request[IO](Method.GET, Uri.unsafeFromString(s"/orders/${UUID.randomUUID().toString}"))
      val routes                            = OrderRoutes[IO](failingOrderService).routes(testAuthMiddleware(au))
      expectFailure(req, routes)
    }
  }
}
