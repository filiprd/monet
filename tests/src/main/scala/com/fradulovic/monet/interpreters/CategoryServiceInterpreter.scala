package com.fradulovic.monet.interpreters

import com.fradulovic.monet.alg.CategoryAlg
import com.fradulovic.monet.domain.*

import cats.effect.IO

object CategoryServiceInterpreter {

  def successfulCategoryService(categories: List[Category]): CategoryAlg[IO] = new CategoryAlg[IO] {
    override def getAll(): IO[List[Category]] = IO.pure(categories)
  }
}
