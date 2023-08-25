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

package com.ritense.plugin.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.crypto.spec.SecretKeySpec

internal class EncryptionServiceTest {
    val oldEncryptedValue = "tRAgOSv2vlAuxA4+igFVyw=="
    val encryptedValue = "{AES/GCM/NoPadding}G5Mfnj95piqF92nudCfAQSzHi3DbTNm+XZ7+cDAIPrE="
    val value = "test"
    val encryptionKey = "abcdefghijklmnop"
    val service = EncryptionService(encryptionKey)

    @Test
    fun `should decrypt old value`() {
        val result = service.decrypt(oldEncryptedValue)
        assertEquals(value, result)
    }

    @Test
    fun `should decrypt value`() {
        val result = service.decrypt(encryptedValue)
        assertEquals(value, result)
    }

    @Test
    fun `should decrypt to same value`() {
        val result = service.decrypt(service.encrypt(value))
        assertEquals(value, result)
    }

}