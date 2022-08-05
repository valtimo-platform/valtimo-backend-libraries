/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
import java.time.LocalDate

internal class CreateDocumentRequestTest{
    @Test
    fun `should serialize inhoud value as base64`() {
        val requestToSerialize = CreateDocumentRequest(
            bronorganisatie = "123",
            creatiedatum = LocalDate.of(2020, 5, 3),
            titel = "titel",
            bestandsnaam = "test",
            taal = "taal",
            inhoud = "test".byteInputStream(),
            informatieobjecttype = "type",
            status = DocumentStatusType.DEFINITIEF
        )
        val output = Mapper.INSTANCE.get().writeValueAsString(requestToSerialize)

        val parsedOutput = Mapper.INSTANCE.get().readValue(output, Map::class.java)

        assertEquals("dGVzdA==", parsedOutput["inhoud"])
        assertEquals("2020-05-03", parsedOutput["creatiedatum"])
    }
}