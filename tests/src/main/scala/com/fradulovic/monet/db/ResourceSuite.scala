package com.fradulovic.monet.db

import cats.effect.*
import cats.syntax.flatMap.*
import weaver.IOSuite
import weaver.scalacheck.{CheckConfig, Checkers}

abstract class ResourceSuite extends IOSuite with Checkers {

  // For testing postgres integration we make only one test, which should be enough
  override def checkConfig: CheckConfig = CheckConfig.default.copy(minimumSuccessful = 1)

  extension (res: Resource[IO, Res]) {
    def beforeAll(f: Res => IO[Unit]): Resource[IO, Res] = res.evalTap(f)
    def afterAll(f: Res => IO[Unit]): Resource[IO, Res]  = res.flatTap(x => Resource.make(IO.unit)(_ => f(x)))
  }
}
