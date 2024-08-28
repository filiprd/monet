package com.fradulovic.monet.gens

import java.net.URI
import java.util.UUID

import scala.annotation.nowarn

import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

import monix.newtypes.HasBuilder
import org.scalacheck.Gen

object MonetGenerators {

  /** using this as a faster alternative to Gen.alphaStr */
  val nonEmptyStringGen: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  private def sized(size: Int): Gen[Long] = {
    def go(s: Int, acc: String): Gen[Long] =
      Gen.oneOf(1 to 9).flatMap { n =>
        if (s == size) acc.toLong
        else go(s + 1, acc + n.toString)
      }
    go(0, "")
  }

  private val uriGen: Gen[URI] = nonEmptyStringGen.map(nes => URI(s"https://$nes.com"))

  /** This is not really type-safe, but in tests it's fine */
  @nowarn
  def idGen[A](using builder: HasBuilder.Aux[A, UUID]): Gen[A] =
    Gen.uuid.map(builder.build(_) match {
      case Right(v) => v
    })

  @nowarn
  def strGen[A](using builder: HasBuilder.Aux[A, String]): Gen[A] =
    nonEmptyStringGen.map(builder.build(_) match {
      case Right(v) => v
    })

  val emailGen: Gen[Email] = nonEmptyStringGen.map(nes => Email(s"$nes@fradulovic.com"))

  val userGen: Gen[User] = for {
    id <- idGen[UserId]
    e  <- emailGen
    p  <- strGen[EncryptedPassword]
    n  <- strGen[Name]
  } yield User(id, e, p, n)

  val categoryGen: Gen[Category] = for {
    id <- idGen[CategoryId]
    l  <- strGen[CategoryLabel]
  } yield Category(id, l)

  val techniqueGen: Gen[Technique] = for {
    id <- idGen[TechniqueId]
    l  <- strGen[TechniqueLabel]
  } yield Technique(id, l)

  val priceGen: Gen[Price] = Gen.posNum[Int].map(Price(_))

  val paintingGen: Gen[Painting] = for {
    id   <- idGen[PaintingId]
    name <- strGen[PaintingName]
    desc <- strGen[PaintingDescription]
    c    <- categoryGen
    t    <- techniqueGen
    imgs <- Gen.nonEmptyListOf(uriGen)
    p    <- priceGen
    uid  <- idGen[UserId]
  } yield Painting(id, name, desc, c, t, imgs, p, uid)

  val creditCardGen: Gen[CreditCard] =
    for {
      nam <- nonEmptyStringGen.map(CreditCardName.unsafe)
      num <- sized(16).map(CreditCardNumber.unsafe)
      exp <- sized(4).map(e => CreditCardExpDate.unsafe(e.toInt))
      cvc <- sized(3).map(c => CVC.unsafe(c.toInt))
    } yield CreditCard(nam, num, exp, cvc)

  val orderGen: Gen[Order] = for {
    oid  <- idGen[OrderId]
    pid  <- idGen[PaymentId]
    uid  <- idGen[UserId]
    pids <- Gen.nonEmptyListOf(idGen[PaintingId]).map(_.toSet)
    p    <- priceGen
  } yield Order(oid, pid, uid, pids, p)

  val cartGen: Gen[Cart] = for {
    uid <- idGen[UserId]
    p   <- Gen.nonEmptyListOf(paintingGen)
    pr  <- priceGen
  } yield Cart(uid, p.toSet, pr)

  val authUserGen: Gen[AuthUser] = for {
    uid <- idGen[UserId]
    e   <- emailGen
  } yield AuthUser(uid, e)

  val paymentGen: Gen[Payment] = for {
    uid   <- idGen[UserId]
    total <- priceGen
    cc    <- creditCardGen
  } yield Payment(uid, total, cc)
}
