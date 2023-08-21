/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.connector.config

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesEncryption(secret: String) {
    private val key = SecretKeySpec(secret.toByteArray(), "AES")
    private val secureRandom = SecureRandom()

    fun encrypt(attribute: String): String {
        return PREFIX.plus(String(Base64.getEncoder().encode(encrypt(attribute, key))))
    }

    fun decrypt(value: String): String {
        return if (value.startsWith(PREFIX)) {
            val valueWithoutPrefix = value.substring(PREFIX.length)
            decrypt(Base64.getDecoder().decode(valueWithoutPrefix), key)
        } else {
            val cipher = createOldDecryptCipher()
            String(cipher.doFinal(Base64.getDecoder().decode(value)))
        }
    }

    fun encrypt(plaintext: String, secretKey: SecretKey): ByteArray? {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
        val parameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

        val cipherText = cipher.doFinal(plaintext.toByteArray())
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(iv.size + cipherText.size)
        byteBuffer.put(iv)
        byteBuffer.put(cipherText)
        return byteBuffer.array()
    }

    fun decrypt(cipherMessage: ByteArray, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
        val gcmIv: AlgorithmParameterSpec = GCMParameterSpec(128, cipherMessage, 0, GCM_IV_LENGTH)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmIv)
        val plainText = cipher.doFinal(cipherMessage, GCM_IV_LENGTH, cipherMessage.size - GCM_IV_LENGTH)
        return String(plainText)
    }

    private fun createOldDecryptCipher(): Cipher {
        val cipher = Cipher.getInstance(AES)
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher
    }

    companion object {
        const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
        const val AES = "AES"
        const val PREFIX = "{AES/GCM/NoPadding}"
        const val GCM_IV_LENGTH = 12
    }
}