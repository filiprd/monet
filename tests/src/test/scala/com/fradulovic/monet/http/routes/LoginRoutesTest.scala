package com.fradulovic.monet.http.routes

import com.fradulovic.monet.domain.LoginUser
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.gens.MonetGenerators.*
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.interpreters.AuthServiceInterpreter

import cats.effect.IO
import dev.profunktor.auth.jwt.JwtToken
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.implicits.*

object LoginRoutesTest extends HttpTestSuite {

  import com.fradulovic.monet.domain.OrphanInstances.jwtTokenEncoder

  test("successful login") {
    forall(emailGen) { case email =>
      val authService   = AuthServiceInterpreter.successfulAuthService(email)
      val req           = Request[IO](Method.POST, uri"/auth/login").withEntity(LoginUser(email, Password("")))
      val routes        = LoginRoutes[IO](authService).routes
      val expectedToken = JwtToken(email.value)
      expectStatusAndBody(req, routes)(Status.Ok, expectedToken)
    }
  }

  test("non-existing user login") {
    forall(emailGen) { case email =>
      val authService = AuthServiceInterpreter.noUsersAuthService(email)
      val req         = Request[IO](Method.POST, uri"/auth/login").withEntity(LoginUser(email, Password("")))
      val routes      = LoginRoutes[IO](authService).routes
      expectStatus(req, routes)(Status.Forbidden)
    }
  }
}
