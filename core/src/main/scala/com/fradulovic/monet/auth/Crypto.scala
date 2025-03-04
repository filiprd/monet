package com.fradulovic.monet.auth

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.*
import javax.crypto.spec.*

import com.fradulovic.monet.auth.AuthTypes.*
import com.fradulovic.monet.conf.ConfigTypes.*
import com.fradulovic.monet.domain.NewTypes.*

import cats.effect.kernel.Sync
import cats.syntax.all.*

trait Crypto {
  def encrypt(pass: Password): EncryptedPassword
  def decrypt(encPass: EncryptedPassword): Password
}

object Crypto {
  def make[F[_]: Sync](passwordSalt: PasswordSalt): F[Crypto] =
    Sync[F].delay {
      val random  = new SecureRandom()
      val ivBytes = new Array[Byte](16)
      random.nextBytes(ivBytes)
      val iv       = new IvParameterSpec(ivBytes);
      val salt     = passwordSalt.value.getBytes("UTF-8")
      val keySpec  = new PBEKeySpec("password".toCharArray(), salt, 65536, 256)
      val factory  = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
      val bytes    = factory.generateSecret(keySpec).getEncoded
      val sKeySpec = new SecretKeySpec(bytes, "AES")
      val eCipher  = EncryptCipher(Cipher.getInstance("AES/CBC/PKCS5Padding"))
      eCipher.value.init(Cipher.ENCRYPT_MODE, sKeySpec, iv)
      val dCipher = DecryptCipher(Cipher.getInstance("AES/CBC/PKCS5Padding"))
      dCipher.value.init(Cipher.DECRYPT_MODE, sKeySpec, iv)
      (eCipher, dCipher)
    }.map { case (ec, dc) =>
      new Crypto {
        def encrypt(password: Password): EncryptedPassword = {
          val base64 = Base64.getEncoder()
          val bytes  = password.value.getBytes("UTF-8")
          val result = new String(base64.encode(ec.value.doFinal(bytes)), "UTF-8")
          EncryptedPassword(result)
        }

        def decrypt(password: EncryptedPassword): Password = {
          val base64 = Base64.getDecoder()
          val bytes  = base64.decode(password.value.getBytes("UTF-8"))
          val result = new String(dc.value.doFinal(bytes), "UTF-8")
          Password(result)
        }
      }
    }
}
