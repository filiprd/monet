package com.fradulovic.monet.http.routes.secured

import com.fradulovic.monet.domain.AuthUser
import com.fradulovic.monet.domain.NewTypes.Email
import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.interpreters.AuthServiceInterpreter

import cats.effect.IO
import org.http4s.*
import org.http4s.implicits.uri
import org.scalacheck.Gen

object LogoutRoutesTest extends HttpTestSuite {

  test("logout") {
    val gen: Gen[(AuthUser, Email)] =
      for {
        au <- authUserGen
        e  <- emailGen
      } yield (au, e)

    forall(gen) { case (au, email) =>
      val authServiceInterpreter = AuthServiceInterpreter.successfulAuthService(email)
      val req =
        Request[IO](Method.POST, uri"/auth/logout") // no need to pass tokens since the interpreter does not use them
      val routes = LogoutRoutes[IO](authServiceInterpreter).routes(testAuthMiddleware(au))
      expectStatus(req, routes)(Status.NoContent)
    }
  }

}
