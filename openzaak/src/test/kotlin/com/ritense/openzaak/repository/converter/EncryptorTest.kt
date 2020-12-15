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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EncryptorTest {

    val valueToEncrypt: String = "ySCrWMK7nCPdoSkjydb58racw2tOzuDqgge3SFhgR3Fe"
    val encryptor = Encryptor("0123456789101112")

    @BeforeEach
    internal fun setUp() {
    }

    @Test
    fun encrypt() {
        val encryptedValue = encryptor.encrypt(valueToEncrypt)
        assertThat(encryptedValue).hasSizeLessThan(128)
    }

    @Test
    fun decrypt() {
        val encryptedValue = encryptor.encrypt(valueToEncrypt)
        assertThat(encryptedValue).hasSizeLessThan(128)

        val decryptedValue = encryptor.decrypt(encryptedValue)
        assertThat(decryptedValue).isEqualTo(valueToEncrypt)
    }

}