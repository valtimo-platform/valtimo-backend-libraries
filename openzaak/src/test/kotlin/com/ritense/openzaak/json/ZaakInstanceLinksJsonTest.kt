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

package com.ritense.openzaak.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandlers
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLinks
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.boot.test.json.JacksonTester
import org.springframework.context.ApplicationEventPublisher
import java.net.URI
import java.util.UUID

class ZaakInstanceLinksJsonTest {

    lateinit var jacksonTester: JacksonTester<ZaakInstanceLinks>
    var zaakInstanceId = UUID.fromString("02e658d0-d5c3-4469-a895-41bbde70635b")
    var documentId = UUID.fromString("5bc545f8-dc94-42cc-bb45-0bf879382df9")
    val zaakType = URI.create("http://example.com")

    private val JSON_STRING_VALUE = """
            [{
                "className": "com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink",
                "zaakInstanceUrl": "http://example.com",
                "zaakInstanceId": "02e658d0-d5c3-4469-a895-41bbde70635b",
                "documentId": "5bc545f8-dc94-42cc-bb45-0bf879382df9"
            }]
    """.trimIndent()

    val applicationEventPublisher = mock(ApplicationEventPublisher::class.java)

    @BeforeEach
    fun setUp() {
        //JacksonTester.initFields(this, Mapper.get())
        JacksonTester.initFields(this, ObjectMapper().findAndRegisterModules())
    }

    @Test
    fun shouldParseJson() {
        val zaakTypeLink = ZaakTypeLink(
            ZaakTypeLinkId.existingId(UUID.randomUUID()),
            "aName",
            zaakType,
            ZaakInstanceLinks(),
            ServiceTaskHandlers()
        )
        zaakTypeLink.assignZaakInstance(
            ZaakInstanceLink(
                URI.create("http://example.com"),
                zaakInstanceId,
                documentId
            ))

        assertThat(jacksonTester.parse(JSON_STRING_VALUE)).isEqualTo(zaakTypeLink.zaakInstanceLinks)
    }

    @Test
    fun shouldMarshalObjectToJson() {
        val zaakTypeLink = ZaakTypeLink(
            ZaakTypeLinkId.existingId(UUID.randomUUID()),
            "aName",
            zaakType,
            ZaakInstanceLinks(),
            ServiceTaskHandlers()
        )
        zaakTypeLink.assignZaakInstance(
            ZaakInstanceLink(
                URI.create("http://example.com"),
                zaakInstanceId,
                documentId
            ))

        val jsonContent = jacksonTester.write(zaakTypeLink.zaakInstanceLinks)

        assertThat(jsonContent).isEqualToJson(JSON_STRING_VALUE)
    }

}