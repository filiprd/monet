package com.fradulovic.monet.retries

/** Defines retriable actions */
sealed trait Retriable

case object RetriableOrder   extends Retriable
case object RetriablePayment extends Retriable
