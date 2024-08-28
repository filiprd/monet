package com.fradulovic.monet.auth

import javax.crypto.Cipher

import monix.newtypes.NewtypeWrapped

object AuthTypes {

  type EncryptCipher = EncryptCipher.Type
  object EncryptCipher extends NewtypeWrapped[Cipher]

  type DecryptCipher = DecryptCipher.Type
  object DecryptCipher extends NewtypeWrapped[Cipher]

}
