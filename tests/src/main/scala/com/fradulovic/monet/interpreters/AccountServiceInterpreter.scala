package com.fradulovic.monet.interpreters

import com.fradulovic.monet.alg.AccountAlg
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

import cats.effect.IO
import cats.syntax.all.*

object AccountServiceInterpreter {

  def successfulAccountService(user: User): AccountAlg[IO] = new AccountAlg[IO] {
    override def createAccount(create: CreateAccount) = IO.pure(user.uuid)
    def retrieveAccount(email: Email) =
      if email === user.email then IO(Some(user)) else IO.pure(Option.empty[User])
  }

  def accountInUseAccountService: AccountAlg[IO] = new AccountAlg[IO] {
    override def createAccount(create: CreateAccount) = AccountInUse.raiseError[IO, UserId]
    def retrieveAccount(email: Email)                 = ???
  }

}
