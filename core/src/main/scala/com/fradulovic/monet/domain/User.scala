package com.fradulovic.monet.domain

import scala.util.control.NoStackTrace

import com.fradulovic.monet.domain.NewTypes.*

import cats.Show
import cats.derived.*
import cats.kernel.Eq
import dev.profunktor.auth.jwt.JwtToken
import io.circe.Codec

case class User(
    uuid: UserId,
    email: Email,
    password: EncryptedPassword,
    name: Name
) derives Eq,
      Codec

object User {
  given userShow: Show[User] = Show.show(u => s"${u.email.value} user named ${u.name}")
}

case class AuthUser(id: UserId, email: Email) derives Show, Codec
object AuthUser {
  def fromUser(user: User): AuthUser = AuthUser(user.uuid, user.email)
}

case object AccountInUse extends NoStackTrace

case class CreateAccount(email: Email, encPassword: EncryptedPassword, name: Name) derives Codec

case object UserNotFound    extends NoStackTrace
case object InvalidPassword extends NoStackTrace

case class LoginUser(email: Email, password: Password) derives Codec

case class LogoutUser(token: JwtToken, email: Email)
