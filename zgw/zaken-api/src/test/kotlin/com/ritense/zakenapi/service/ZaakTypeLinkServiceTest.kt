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

package com.ritense.zakenapi.service

import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinition
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinitionId
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.zakenapi.domain.ZaakTypeLink
import com.ritense.zakenapi.domain.ZaakTypeLinkId
import com.ritense.zakenapi.repository.ZaakTypeLinkRepository
import com.ritense.zakenapi.web.rest.request.CreateZaakTypeLinkRequest
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.Optional
import java.util.UUID

class ZaakTypeLinkServiceTest {

    val documentDefinitionName = "testDocumentDefinitionName"
    val invalidDocumentDefinitionName = "definitelywaymorecharactersthanallowedforadocumentdefinitionname"

    lateinit var zaakTypeLinkService: ZaakTypeLinkService
    lateinit var zaakTypeLink: ZaakTypeLink
    lateinit var zaakTypeLinkId: ZaakTypeLinkId

    @Mock
    lateinit var zaakTypeLinkRepository: ZaakTypeLinkRepository

    @Mock
    lateinit var processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService

    @Mock
    lateinit var documentDefinitionService: JsonSchemaDocumentDefinitionService

    val zaakTypeUrl = URI.create("http//example.com")
    val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        zaakTypeLinkService = DefaultZaakTypeLinkService(
            zaakTypeLinkRepository,
            processDefinitionCaseDefinitionService,
            documentDefinitionService
        )
        zaakTypeLinkId = ZaakTypeLinkId.newId(UUID.randomUUID())

        zaakTypeLink = ZaakTypeLink(
            zaakTypeLinkId,
            caseDefinitionId,
            zaakTypeUrl
        )
        whenever(zaakTypeLinkRepository.findById(zaakTypeLinkId)).thenReturn(Optional.of(zaakTypeLink))

    }

    @Test
    fun `should return entity`() {
        whenever(zaakTypeLinkRepository.findByCaseDefinitionId(caseDefinitionId)).thenReturn(zaakTypeLink)

        val result = zaakTypeLinkService.get(caseDefinitionId)

        assertThat(result?.caseDefinitionId).isEqualTo(zaakTypeLink.caseDefinitionId)
        assertThat(result?.zaakTypeUrl).isEqualTo(zaakTypeLink.zaakTypeUrl)
    }

    @Test
    fun `should create entity`() {
        val request = CreateZaakTypeLinkRequest(
            zaakTypeUrl
        )

        whenever(zaakTypeLinkRepository.save(any())).thenAnswer { invocation -> invocation.getArgument<ZaakTypeLink>(0) }

        val result = zaakTypeLinkService.createZaakTypeLink(caseDefinitionId, request)

        assertThat(result.caseDefinitionId).isEqualTo(caseDefinitionId)
        assertThat(result.zaakTypeUrl).isEqualTo(zaakTypeUrl)
    }

    @Test
    fun `should get zaakTypeLink`() {

        whenever(
            processDefinitionCaseDefinitionService.findByProcessDefinitionId(
                ProcessDefinitionId("123")
            )
        )
            .thenReturn(
                ProcessDefinitionCaseDefinition(
                    ProcessDefinitionCaseDefinitionId(
                        ProcessDefinitionId("123"),
                        caseDefinitionId
                    ),
                    true,
                    false
                )
            )

        whenever(
            documentDefinitionService.findByCaseDefinitionId(
                caseDefinitionId
            )
        )
            .thenReturn(
                Optional.of(
                    JsonSchemaDocumentDefinition(
                        JsonSchemaDocumentDefinitionId.of(
                            "123",
                            caseDefinitionId
                        ),
                        JsonSchema.fromString(
                            """
                                {
                                    "${'$'}id": "123.schema",
                                    "${'$'}schema": "http://json-schema.org/draft-07/schema#",
                                    "title": "additional-property-example",
                                    "type": "object",
                                    "additionalProperties": true
                                }
                            """.trimIndent()
                        )
                    )
                )
            )

        whenever(zaakTypeLinkRepository.findByCaseDefinitionId(caseDefinitionId))
            .thenReturn(zaakTypeLink)

        val result = zaakTypeLinkService.getByProcess("123")

        assertThat(result).isEqualTo(zaakTypeLink)
    }

}
