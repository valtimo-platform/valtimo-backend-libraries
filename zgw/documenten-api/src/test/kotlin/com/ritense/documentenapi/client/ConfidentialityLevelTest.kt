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

package com.ritense.documentenapi.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.zgw.domain.Vertrouwelijkheid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConfidentialityLevelTest {

    @Test
    fun `should serialize enum to key`() {
        val jsonValue = jacksonObjectMapper().writeValueAsString(Vertrouwelijkheid.VERTROUWELIJK)
        assertEquals(""" "vertrouwelijk" """.trim(), jsonValue)
    }

    @Test
    fun `should deserialize key to enum`() {
        val enumValue = jacksonObjectMapper().readValue(""" "vertrouwelijk" """.trim(), Vertrouwelijkheid::class.java)
        assertEquals(Vertrouwelijkheid.VERTROUWELIJK, enumValue)
    }
}