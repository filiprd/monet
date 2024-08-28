package com.fradulovic.monet.http.auth

import dev.profunktor.auth.jwt.JwtSymmetricAuth
import monix.newtypes.NewtypeWrapped
import monix.newtypes.integrations.DerivedCirceCodec

object AuthTypes {

  type UserJwtAuth = UserJwtAuth.Type
  object UserJwtAuth extends NewtypeWrapped[JwtSymmetricAuth] with DerivedCirceCodec
}
