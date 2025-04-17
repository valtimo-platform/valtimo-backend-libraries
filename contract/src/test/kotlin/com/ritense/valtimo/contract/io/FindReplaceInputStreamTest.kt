/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.io

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.text.Charsets.UTF_8

class FindReplaceInputStreamTest {

    @Test
    fun `should find and replace values`() {
        val inputStream = "find-01234+find-56789+find".byteInputStream(UTF_8)

        val newIn = FindReplaceInputStream(inputStream, "find", "replace")

        val result = newIn.bufferedReader().use { it.readText() }
        assertThat(result).isEqualTo("replace-01234+replace-56789+replace")
    }

    @Test
    fun `should not replace partial matches`() {
        val inputStream = "finfind-01234+fin-56789+fin".byteInputStream(UTF_8)

        val newIn = FindReplaceInputStream(inputStream, "find", "replace")

        val result = newIn.bufferedReader().use { it.readText() }
        assertThat(result).isEqualTo("finreplace-01234+fin-56789+fin")
    }

    @Test
    fun `should not support overlapping values`() {
        val inputStream = "foo1234567890".byteInputStream(UTF_8)

        val exception = assertThrows<UnsupportedOperationException> {
            FindReplaceInputStream(inputStream, "foo", "foofoo")
        }

        assertEquals("Overlapping values are not supported", exception.message)
    }
}