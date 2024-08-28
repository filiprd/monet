package com.fradulovic.monet.ext.http4s

import cats.MonadThrow
import cats.syntax.all.*
import io.circe.Decoder
import org.http4s.*
import org.http4s.circe.{JsonDecoder, *}
import org.http4s.dsl.Http4sDsl

object Refined {
  extension [F[_]: JsonDecoder: MonadThrow](req: Request[F]) {

    /** Allows to decode http requests and to deal with validation errors. The
      * idea is to turn unprocessable entity responses into bad request
      * responses
      */
    def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] = RefinedDecoder().decode(req)(f)
  }
}

final private case class RefinedDecoder[F[_]: JsonDecoder: MonadThrow, A: Decoder]() extends Http4sDsl[F] {
  def decode(req: Request[F])(f: A => F[Response[F]]): F[Response[F]] =
    req.asJsonDecode[A].attempt.flatMap {
      case Left(e) =>
        Option(e.getCause) match {
          case Some(c) if c.getMessage.contains("DecodingFailure") => BadRequest(c.getMessage)
          case _                                                   => UnprocessableEntity()
        }
      case Right(a) => f(a)

    }
}
