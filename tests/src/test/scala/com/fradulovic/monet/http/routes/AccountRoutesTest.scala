package com.fradulovic.monet.http.routes

import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.domain.User
import com.fradulovic.monet.gens.MonetGenerators
import com.fradulovic.monet.http.HttpTestSuite
import com.fradulovic.monet.http.req.*
import com.fradulovic.monet.http.req.ParamTypes.*
import com.fradulovic.monet.interpreters.{AccountServiceInterpreter, CryptoInterpreter}

import cats.Show
import cats.effect.IO
import io.circe.Encoder
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.implicits.uri
import org.http4s.{Method, Request, Status}
import org.scalacheck.Gen

object AccountRoutesTest extends HttpTestSuite {

  val crypto = CryptoInterpreter.crypto()

  val gen: Gen[(User, CreateAccountReq)] = MonetGenerators.userGen.map(user =>
    user ->
      CreateAccountReq(
        EmailParam(user.email.value).toOption.get,
        PasswordParam(user.password.value).toOption.get,
        NameParam(user.name.value).toOption.get
      )
  )

  test("successful account creation") {
    forall(gen) { case (user, createAccountReq) =>
      val accountService = AccountServiceInterpreter.successfulAccountService(user)
      val req            = Request[IO](Method.POST, uri"/account").withEntity(createAccountReq)
      val routes         = AccountRoutes(accountService, crypto).routes
      expectStatusAndBody(req, routes)(Status.Ok, user.uuid)
    }
  }

  test("account in use -> conflict") {
    forall(gen) { case (_, createAccountReq) =>
      val accountService = AccountServiceInterpreter.accountInUseAccountService
      val req            = Request[IO](Method.POST, uri"/account").withEntity(createAccountReq)
      val routes         = AccountRoutes(accountService, crypto).routes
      expectStatus(req, routes)(Status.Conflict)
    }
  }

  test("misspelled body param req") {
    forall(MonetGenerators.userGen) { case user =>
      import io.circe.literal.*
      val accountService = AccountServiceInterpreter.successfulAccountService(user)
      val req = Request[IO](Method.POST, uri"/account").withEntity(
        json"""{"uername":"test", "email": "test@fradulovic.com", "password": "pass", "name": "name"}"""
      )
      val routes = AccountRoutes(accountService, crypto).routes
      expectStatus(req, routes)(Status.BadRequest)
    }
  }

  test("wrong type in body param req") {
    forall(MonetGenerators.userGen) { case user =>
      import io.circe.literal.*
      val accountService = AccountServiceInterpreter.successfulAccountService(user)
      val req = Request[IO](Method.POST, uri"/account").withEntity(
        json"""{"email": "test@fradulovic.com", "password": "pass", "name": 25}"""
      )
      val routes = AccountRoutes(accountService, crypto).routes
      expectStatus(req, routes)(Status.BadRequest)
    }
  }

  test("wrong email format in body req") {
    forall(MonetGenerators.userGen) { case user =>
      import io.circe.literal.*
      val accountService = AccountServiceInterpreter.successfulAccountService(user)
      val req = Request[IO](Method.POST, uri"/account").withEntity(
        json"""{"email": "testfradulovic.com", "password": "pass", "name": "Filip"}"""
      )
      val routes = AccountRoutes(accountService, crypto).routes
      expectStatus(req, routes)(Status.BadRequest)
    }
  }
}
