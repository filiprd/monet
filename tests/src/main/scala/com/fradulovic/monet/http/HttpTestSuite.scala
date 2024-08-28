package com.fradulovic.monet.http

import com.fradulovic.monet.domain.AuthUser

import cats.data.Kleisli
import cats.effect.IO
import fs2.Stream
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.{Decoder, Encoder, Json}
import org.http4s.*
import org.http4s.circe.*
import org.http4s.server.AuthMiddleware
import org.typelevel.jawn.Facade
import org.typelevel.jawn.fs2.*
import weaver.scalacheck.Checkers
import weaver.{Expectations, SimpleIOSuite}

trait HttpTestSuite extends SimpleIOSuite with Checkers {

  given facade: Facade[Json] = new io.circe.jawn.CirceSupportParser(None, false).facade

  def expectStatusAndBody[A: Encoder](
      req: Request[IO],
      routes: HttpRoutes[IO]
  )(status: org.http4s.Status, body: A): IO[Expectations] =
    routes.run(req).value.flatMap {
      case Some(resp) =>
        resp.asJson.map { jResp =>
          expect.same(jResp.dropNullValues, body.asJson.dropNullValues) and expect.same(resp.status, status)
        }
      case None => IO.pure(failure("request not successful"))
    }

  def expectStatusAndStream[A: Encoder: Decoder](
      req: Request[IO],
      routes: HttpRoutes[IO]
  )(status: org.http4s.Status, body: List[A]): IO[Expectations] =
    routes.run(req).value.flatMap {
      case Some(resp) =>
        resp.body.chunks.parseJsonStream
          .through(_.flatMap { json =>
            decode[A](json.noSpaces) match {
              case Left(e)  => Stream.raiseError(e)
              case Right(a) => Stream.emit(a)
            }
          })
          .compile
          .toList
          .map(la => expect.same(la, body) and expect.same(resp.status, status))
      case None => IO.pure(failure("request not successful"))
    }

  def expectStatus(req: Request[IO], routes: HttpRoutes[IO])(status: org.http4s.Status): IO[Expectations] =
    routes.run(req).value.map {
      case Some(resp) => expect.same(resp.status, status)
      case None       => failure("status not as expected")
    }

  def expectFailure(
      req: Request[IO],
      routes: HttpRoutes[IO]
  ): IO[Expectations] =
    routes.run(req).value.attempt.map {
      case Left(_)  => success
      case Right(_) => failure("expected a failure but got success")
    }

  def expectFailedStream(
      req: Request[IO],
      routes: HttpRoutes[IO]
  ): IO[Expectations] =
    routes.run(req).value.flatMap {
      case Some(resp) =>
        resp.body.chunks.parseJsonStream.compile.drain
          .map(_ => failure("expected a failure but got success"))
          .recover(_ => success)
      case None => IO.pure(failure("expected a failure but got success"))
    }

  def testAuthMiddleware(user: AuthUser): AuthMiddleware[IO, AuthUser] =
    AuthMiddleware(Kleisli.pure(user))
}
