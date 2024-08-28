package com.fradulovic.monet.conf

enum MonetEnvironment {
  case Prod, Test
}

object MonetEnvironment {
  def fromString(s: String): Option[MonetEnvironment] = s match {
    case "prod" => Some(Prod)
    case "test" => Some(Test)
    case _      => None
  }
}
