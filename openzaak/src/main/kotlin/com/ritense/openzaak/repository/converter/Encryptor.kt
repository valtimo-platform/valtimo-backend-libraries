/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.repository.converter

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Encryptor(superSecret: String) {
    private var key = SecretKeySpec(superSecret.toByteArray(), AES)
    private var cipher = Cipher.getInstance(AES)

    fun encrypt(attribute: String): String {
        return try {
            cipher.init(Cipher.ENCRYPT_MODE, key)
            Base64.getEncoder().encodeToString(cipher.doFinal(attribute.toByteArray()))
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    fun decrypt(value: String): String {
        return try {
            cipher.init(Cipher.DECRYPT_MODE, key)
            String(cipher.doFinal(Base64.getDecoder().decode(value)))
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    companion object {
        const val AES = "AES"
    }
}