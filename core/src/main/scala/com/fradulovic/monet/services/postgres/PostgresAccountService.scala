package com.fradulovic.monet.services.postgres

import com.fradulovic.monet.alg.AccountAlg
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*
import com.fradulovic.monet.effects.GenUUID

import cats.effect.kernel.{MonadCancelThrow, Resource}
import cats.syntax.all.*
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

import SharedCodecs.*

object PostgresAccountService {

  def make[F[_]: GenUUID: MonadCancelThrow](postgres: Resource[F, Session[F]]): AccountAlg[F] =
    new AccountAlg[F] {
      import AccountSQL.*

      override def createAccount(create: CreateAccount): F[UserId] =
        postgres.use(session =>
          ID.make[F, UserId]
            .flatMap(uuid =>
              session
                .prepareR(insertUser)
                .use(_.execute(uuid ~ create).as(uuid).recoverWith { case SqlState.UniqueViolation(_) =>
                  AccountInUse.raiseError[F, UserId]
                })
            )
        )

      override def retrieveAccount(email: Email): F[Option[User]] =
        postgres.use(_.prepareR(selectUser).use(_.option(email)))
    }
}

private object AccountSQL {
  val email: Codec[Email]                = varchar.imap(Email(_))(_.value)
  val password: Codec[EncryptedPassword] = varchar.imap(EncryptedPassword(_))(_.value)
  val name: Codec[Name]                  = varchar.imap(Name(_))(_.value)
  val decoder: Decoder[User] = (userId ~ email ~ password ~ name).map { case id ~ e ~ p ~ n =>
    User(id, e, p, n)
  }

  val insertUser: Command[UserId ~ CreateAccount] =
    sql"""
         INSERT INTO users
         VALUES ($userId, $email, $password, $name)
       """.command.contramap { case id ~ ca =>
      (id, ca.email, ca.encPassword, ca.name)
    }
  val selectUser: Query[Email, User] =
    sql"SELECT * FROM users WHERE email = $email".query(decoder)

}
