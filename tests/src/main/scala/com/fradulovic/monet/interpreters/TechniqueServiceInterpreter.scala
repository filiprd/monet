package com.fradulovic.monet.interpreters

import com.fradulovic.monet.alg.TechniqueAlg
import com.fradulovic.monet.domain.*

import cats.effect.IO

object TechniqueServiceInterpreter {

  def successfulTechniqueService(techniques: List[Technique]): TechniqueAlg[IO] = new TechniqueAlg[IO] {
    override def getAll(): IO[List[Technique]] = IO.pure(techniques)
  }
}
