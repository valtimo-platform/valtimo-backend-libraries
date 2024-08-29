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

import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinition
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinitionId
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentAssociationService
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
import org.mockito.kotlin.eq
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
    lateinit var processDocumentAssociationService: CamundaProcessJsonSchemaDocumentAssociationService

    val zaakTypeUrl = URI.create("http//example.com")

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        zaakTypeLinkService = DefaultZaakTypeLinkService(zaakTypeLinkRepository, processDocumentAssociationService)
        zaakTypeLinkId = ZaakTypeLinkId.newId(UUID.randomUUID())

        zaakTypeLink = ZaakTypeLink(
            zaakTypeLinkId,
            documentDefinitionName,
            zaakTypeUrl
        )
        whenever(zaakTypeLinkRepository.findById(zaakTypeLinkId)).thenReturn(Optional.of(zaakTypeLink))

    }

    @Test
    fun `should return entity`() {
        whenever(zaakTypeLinkRepository.findByDocumentDefinitionName("documentDefinitionName")).thenReturn(zaakTypeLink)

        val result = zaakTypeLinkService.get("documentDefinitionName")

        assertThat(result?.documentDefinitionName).isEqualTo(zaakTypeLink.documentDefinitionName)
        assertThat(result?.zaakTypeUrl).isEqualTo(zaakTypeLink.zaakTypeUrl)
    }

    @Test
    fun `should create entity`() {
        val request = CreateZaakTypeLinkRequest(
            documentDefinitionName,
            zaakTypeUrl
        )

        whenever(zaakTypeLinkRepository.save(any())).thenAnswer { invocation -> invocation.getArgument<ZaakTypeLink>(0)}

        val result = zaakTypeLinkService.createZaakTypeLink(request)

        assertThat(result.documentDefinitionName).isEqualTo(documentDefinitionName)
        assertThat(result.zaakTypeUrl).isEqualTo(zaakTypeUrl)
    }

    @Test
    fun `should not create entity`() {
        val request = CreateZaakTypeLinkRequest(
            invalidDocumentDefinitionName,
            zaakTypeUrl
        )

        assertThrows<ConstraintViolationException> {
            zaakTypeLinkService.createZaakTypeLink(request)
        }
    }

    @Test
    fun `should get empty zaakTypeLinks`() {
        val result = zaakTypeLinkService.getByProcess("processDefinitionKey")

        assertThat(result).isEmpty()
    }

    @Test
    fun `should get zaakTypeLinks`() {
        whenever(processDocumentAssociationService.findAllProcessDocumentDefinitions(CamundaProcessDefinitionKey("processDefinitionKey")))
            .thenReturn(listOf(
                CamundaProcessJsonSchemaDocumentDefinition(
                    CamundaProcessJsonSchemaDocumentDefinitionId.newId(
                        CamundaProcessDefinitionKey("processDefinitionKey"),
                        JsonSchemaDocumentDefinitionId.newId("documentDefinitionId")
                    ),
                    true,
                    false
                ),
                CamundaProcessJsonSchemaDocumentDefinition(
                    CamundaProcessJsonSchemaDocumentDefinitionId.newId(
                        CamundaProcessDefinitionKey("processDefinitionKey"),
                        JsonSchemaDocumentDefinitionId.newId("documentDefinitionId2")
                    ),
                    true,
                    false
                )
            ))

        whenever(zaakTypeLinkRepository.findByDocumentDefinitionNameIn(
            eq(listOf("documentDefinitionId", "documentDefinitionId2")))
        ).thenReturn(listOf(
            zaakTypeLink
        ))

        val result = zaakTypeLinkService.getByProcess("processDefinitionKey")

        assertThat(result).contains(zaakTypeLink)
    }

}
