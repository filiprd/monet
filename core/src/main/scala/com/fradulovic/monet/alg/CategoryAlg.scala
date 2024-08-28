package com.fradulovic.monet.alg

import com.fradulovic.monet.domain.Category

trait CategoryAlg[F[_]] {
  def getAll(): F[List[Category]]
}
