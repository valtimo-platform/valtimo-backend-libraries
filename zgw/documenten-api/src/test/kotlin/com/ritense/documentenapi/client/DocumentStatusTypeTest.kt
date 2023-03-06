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

import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DocumentStatusTypeTest {

    @Test
    fun `should serialize as api value`() {
        val output = Mapper.INSTANCE.get().writeValueAsString(Wrapper(DocumentStatusType.DEFINITIEF))
        assertEquals("{\"status\":\"definitief\"}", output)
    }

    @Test
    fun `should deserialize from api value`() {
        val input = "{\"status\":\"definitief\"}"
        val output = Mapper.INSTANCE.get().readValue(input, Wrapper::class.java)
        assertEquals(DocumentStatusType.DEFINITIEF, output.status)
    }

    internal class Wrapper(
        val status: DocumentStatusType
    )
}