package com.fradulovic.monet.http.routes

import com.fradulovic.monet.alg.AccountAlg
import com.fradulovic.monet.auth.Crypto
import com.fradulovic.monet.domain.AccountInUse
import com.fradulovic.monet.ext.http4s.Refined.*
import com.fradulovic.monet.http.req.CreateAccountReq
import com.fradulovic.monet.http.req.ParamTypes.PasswordParam

import cats.MonadThrow
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class AccountRoutes[F[_]: JsonDecoder: MonadThrow](
    accounts: AccountAlg[F],
    crypto: Crypto
) extends Http4sDsl[F] {

  private val prefixPath = "/account"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root =>
    req.decodeR[CreateAccountReq] { createAccountReq =>
      val encryptedPass = crypto.encrypt(createAccountReq.password.toDomain)
      accounts
        .createAccount(createAccountReq.toDomain(encryptedPass))
        .flatMap(Ok(_))
        .recoverWith { case AccountInUse =>
          Conflict("Account already exists")
        }
    }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
