package com.fradulovic.monet.services.postgres

import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

import skunk.*
import skunk.codec.all.*

object SharedCodecs {
  val techniqueId: Codec[TechniqueId]       = uuid.imap(TechniqueId(_))(_.value)
  val techniqueLabel: Codec[TechniqueLabel] = varchar.imap(TechniqueLabel(_))(_.value)
  val technique: Decoder[Technique]         = (techniqueId ~ techniqueLabel).map { case id ~ label => Technique(id, label) }

  val categoryId: Codec[CategoryId]       = uuid.imap(CategoryId(_))(_.value)
  val categoryLabel: Codec[CategoryLabel] = varchar.imap(CategoryLabel(_))(_.value)
  val category: Decoder[Category]         = (categoryId ~ categoryLabel).map { case id ~ label => Category(id, label) }

  val userId: Codec[UserId]         = uuid.imap(UserId(_))(_.value)
  val paymentId: Codec[PaymentId]   = uuid.imap(PaymentId(_))(_.value)
  val paintingId: Codec[PaintingId] = uuid.imap(PaintingId(_))(_.value)

  val price: Codec[Price] = int4.imap(Price(_))(_.value)

}
