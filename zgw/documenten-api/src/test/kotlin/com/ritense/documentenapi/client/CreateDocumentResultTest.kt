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

package com.ritense.documentenapi.client

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class CreateDocumentResultTest {

    @Test
    fun `should get uuid from url`() {
        val result = CreateDocumentResult(
            "https://www.example.com/847789d3-a8b3-469a-ae01-a49a6bd21783",
            "",
            "",
            0L,
            LocalDateTime.now(),
            listOf()
        )

        assertEquals("847789d3-a8b3-469a-ae01-a49a6bd21783", result.getDocumentUUIDFromUrl())
    }

    @Test
    fun `should get lock from bestanddelen`() {
        val bestandsdeel = Bestandsdeel(
            "https://www.example.com/",
            0,
            0,
            true,
            "847789d3-a8b3-469a-ae01-a49a6bd21783"
        )
        val result = CreateDocumentResult(
            "",
            "",
            "",
            0L,
            LocalDateTime.now(),
            listOf(bestandsdeel)
        )

        assertEquals("847789d3-a8b3-469a-ae01-a49a6bd21783", result.getLockFromBestandsdelen())
    }

    @Test
    fun `should fail gracefully for lock when there are no bestandsdelen`() {
        val result = CreateDocumentResult(
            "",
            "",
            "",
            0L,
            LocalDateTime.now(),
            listOf()
        )

        assertEquals("", result.getLockFromBestandsdelen())
    }

}