package com.fradulovic.monet.http.req

import com.fradulovic.monet.domain.CreateAccount
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.http.req.ParamTypes.*

import cats.Show
import cats.derived.*
import io.circe.Codec

case class CreateAccountReq(
    email: EmailParam,
    password: PasswordParam,
    name: NameParam
) derives Show,
      Codec {
  def toDomain(encPassword: EncryptedPassword): CreateAccount =
    CreateAccount(
      Email(email.value),
      encPassword,
      Name(name.value)
    )
}
