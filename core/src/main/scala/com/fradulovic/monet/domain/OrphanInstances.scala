package com.fradulovic.monet.domain

import cats.Show
import dev.profunktor.auth.jwt.JwtToken
import io.circe.Encoder

object OrphanInstances {

  given jwtTokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  given jwtTokenShow: Show[JwtToken] =
    Show.show[JwtToken](_.value)
}
