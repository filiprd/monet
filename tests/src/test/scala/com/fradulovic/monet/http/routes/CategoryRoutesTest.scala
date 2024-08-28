package com.fradulovic.monet.http.routes

import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.interpreters.CategoryServiceInterpreter

import cats.effect.IO
import org.http4s.*
import org.http4s.implicits.*
import org.scalacheck.Gen

object CategoryRoutesTest extends HttpTestSuite {

  test("get all categories") {
    forall(Gen.nonEmptyListOf(categoryGen)) { case categories =>
      val categoryService = CategoryServiceInterpreter.successfulCategoryService(categories)
      val req             = Request[IO](Method.GET, uri"/categories")
      val routes          = CategoryRoutes[IO](categoryService).routes
      expectStatusAndBody(req, routes)(Status.Ok, categories)
    }
  }
}
