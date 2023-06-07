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

package com.ritense.smartdocuments.io

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.text.Charsets.UTF_8

class SubInputStreamTest {

    @Test
    fun `should only read part of input stream`() {
        val inputStream = "0123456789".byteInputStream(UTF_8)

        val subIn = SubInputStream(inputStream, 2, 6)

        val result = subIn.bufferedReader().use { it.readText() }
        assertThat(result).isEqualTo("2345")
    }
}