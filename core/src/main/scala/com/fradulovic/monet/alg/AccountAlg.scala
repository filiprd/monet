package com.fradulovic.monet.alg

import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.domain.{CreateAccount, User}

trait AccountAlg[F[_]] {
  def createAccount(create: CreateAccount): F[UserId]
  def retrieveAccount(email: Email): F[Option[User]]
}
