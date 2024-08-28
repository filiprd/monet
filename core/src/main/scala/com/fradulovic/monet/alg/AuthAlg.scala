package com.fradulovic.monet.alg

import com.fradulovic.monet.domain.{AuthUser, LoginUser, LogoutUser}

import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim

trait AuthAlg[F[_]] {
  def login(login: LoginUser): F[JwtToken]
  def logout(logout: LogoutUser): F[Unit]
  def findLoggedUser(token: JwtToken)(claim: JwtClaim): F[Option[AuthUser]]
}
