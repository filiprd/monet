package com.fradulovic.monet.interpreters

import com.fradulovic.monet.alg.AuthAlg
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

import cats.effect.IO
import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim

object AuthServiceInterpreter {

  def successfulAuthService(email: Email): AuthAlg[IO] = new AuthAlg[IO] {
    override def login(login: LoginUser): IO[JwtToken]                                  = IO.pure(JwtToken(email.value))
    override def logout(logout: LogoutUser): IO[Unit]                                   = IO.unit
    override def findLoggedUser(token: JwtToken)(claim: JwtClaim): IO[Option[AuthUser]] = ???
  }

  def noUsersAuthService(email: Email): AuthAlg[IO] = new AuthAlg[IO] {
    override def login(login: LoginUser): IO[JwtToken]                                  = IO.raiseError[JwtToken](UserNotFound)
    override def logout(logout: LogoutUser): IO[Unit]                                   = ???
    override def findLoggedUser(token: JwtToken)(claim: JwtClaim): IO[Option[AuthUser]] = ???
  }
}
