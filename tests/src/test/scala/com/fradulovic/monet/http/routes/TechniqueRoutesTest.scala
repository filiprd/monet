package com.fradulovic.monet.http.routes

import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.interpreters.TechniqueServiceInterpreter

import cats.effect.IO
import org.http4s.implicits.*
import org.http4s.{Method, Request, Status}
import org.scalacheck.Gen

object TechniqueRoutesTest extends HttpTestSuite {

  test("get all techniques") {
    forall(Gen.nonEmptyListOf(techniqueGen)) { case techniques =>
      val techniqueService = TechniqueServiceInterpreter.successfulTechniqueService(techniques)
      val req              = Request[IO](Method.GET, uri"/techniques")
      val routes           = TechniqueRoutes[IO](techniqueService).routes
      expectStatusAndBody(req, routes)(Status.Ok, techniques)
    }
  }
}
