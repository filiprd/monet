package com.fradulovic.monet.alg

import com.fradulovic.monet.domain.AppStatus

trait HealthCheckAlg[F[_]] {
  def status: F[AppStatus]
}
