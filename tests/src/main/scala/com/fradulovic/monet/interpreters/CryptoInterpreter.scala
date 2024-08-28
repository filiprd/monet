package com.fradulovic.monet.interpreters

import com.fradulovic.monet.auth.Crypto
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.domain.NewTypes.*

object CryptoInterpreter {

  def crypto(): Crypto = new Crypto {
    def encrypt(pass: Password): EncryptedPassword    = EncryptedPassword(pass.value)
    def decrypt(encPass: EncryptedPassword): Password = Password(encPass.value)
  }
}
