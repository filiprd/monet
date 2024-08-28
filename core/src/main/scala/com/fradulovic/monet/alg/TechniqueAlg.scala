package com.fradulovic.monet.alg

import com.fradulovic.monet.domain.Technique

trait TechniqueAlg[F[_]] {
  def getAll(): F[List[Technique]]
}
