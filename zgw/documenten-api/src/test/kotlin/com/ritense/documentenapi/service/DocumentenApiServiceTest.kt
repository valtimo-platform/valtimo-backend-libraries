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

package com.ritense.documentenapi.service

import com.ritense.authorization.AuthorizationService
import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.domain.DocumentenApiVersion
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.repository.DocumentenApiUploadFieldRepository
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.service.PluginService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.zgw.Rsin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class DocumentenApiServiceTest {
    private lateinit var service: DocumentenApiService

    private lateinit var pluginService: PluginService
    private lateinit var catalogiService: CatalogiService
    private lateinit var documentenApiColumnRepository: DocumentenApiColumnRepository
    private lateinit var documentenApiUploadFieldRepository: DocumentenApiUploadFieldRepository
    private lateinit var authorizationService: AuthorizationService
    private lateinit var valtimoDocumentService: DocumentService
    private lateinit var documentDefinitionService: JsonSchemaDocumentDefinitionService
    private lateinit var documentenApiVersionService: DocumentenApiVersionService
    private lateinit var valueResolverService: ValueResolverService

    @BeforeEach
    fun before() {
        pluginService = mock<PluginService>()
        catalogiService = mock<CatalogiService>()
        documentenApiColumnRepository = mock<DocumentenApiColumnRepository>()
        documentenApiUploadFieldRepository = mock<DocumentenApiUploadFieldRepository>()
        authorizationService = mock<AuthorizationService>()
        valtimoDocumentService = mock<DocumentService>()
        documentDefinitionService = mock<JsonSchemaDocumentDefinitionService>()
        documentenApiVersionService = mock<DocumentenApiVersionService>()
        valueResolverService = mock<ValueResolverService>()

        service = DocumentenApiService(
            pluginService,
            catalogiService,
            documentenApiColumnRepository,
            documentenApiUploadFieldRepository,
            authorizationService,
            valtimoDocumentService,
            documentDefinitionService,
            documentenApiVersionService,
            valueResolverService,
        )
    }

    @Test
    fun `getCaseInformatieObjecten should get informatieobjecten`() {
        val documentId = UUID.randomUUID()
        val documentSearchRequest = DocumentSearchRequest()
        val pageable = Pageable.unpaged()

        val documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId("some-document-name", 3)
        val document = mock<Document>()
        whenever(valtimoDocumentService.get(documentId.toString())).thenReturn(document)
        whenever(document.definitionId()).thenReturn(documentDefinitionId)
        val pluginConfigurationId = PluginConfigurationId.existingId(UUID.randomUUID())
        val pluginConfiguration = mock<PluginConfiguration>()
        val pluginDefinition = mock<PluginDefinition>()
        whenever(pluginDefinition.key).thenReturn(DocumentenApiPlugin.PLUGIN_KEY)
        whenever(pluginConfiguration.pluginDefinition).thenReturn(pluginDefinition)
        whenever(pluginConfiguration.id).thenReturn(pluginConfigurationId)
        val plugin = mock<DocumentenApiPlugin>()
        val relatedFile = createDocumentInformatieObject()
        val page = PageImpl(listOf(relatedFile), pageable, 1)
        whenever(plugin.getInformatieObjecten(documentSearchRequest, pageable)).thenReturn(page)
        val version = DocumentenApiVersion("1.5.0-test-1.0.0", listOf("titel"), listOf("titel"))
        whenever(documentenApiVersionService.getPluginVersion("some-document-name"))
            .thenReturn(Triple(pluginConfiguration, plugin, version))

        val resultPage = service.getCaseInformatieObjecten(documentId, documentSearchRequest, pageable)

        assertEquals(1, resultPage.size)
        val firstResult = resultPage.content.first()
        assertInstanceOf(DocumentInformatieObject::class.java, firstResult)
        firstResult as DocumentInformatieObject
        assertEquals("nl", firstResult.taal)
        assertEquals("titel", firstResult.titel)
        assertEquals(1, firstResult.versie)
        assertEquals("http://localhost/informatieobjecttype", firstResult.informatieobjecttype)
        assertEquals("y", firstResult.auteur)
    }

    @Test
    fun `should call plugin to delete informatie object`() {
        val documentUrl = URI("http://some.url")
        val pluginInstance = mock<DocumentenApiPlugin>()
        whenever(pluginService.createInstance<DocumentenApiPlugin>(any(), any())).thenReturn(pluginInstance)

        service.deleteInformatieObject(documentUrl)

        verify(pluginInstance).deleteInformatieObject(documentUrl)
    }

    @Test
    fun `should throw exception when trying to delete informatie object but plugin does not exist`() {
        val documentUrl = URI("http://some.url")
        whenever(pluginService.createInstance<DocumentenApiPlugin>(any(), any())).thenReturn(null)

        val ex = assertThrows<IllegalArgumentException> {
            service.deleteInformatieObject(documentUrl)
        }

        assertEquals("Trying to delete informatie object by url, but could not find DocumentenApiPlugin instance for informatieobjectUrl http://some.url", ex.message)
    }

    private fun createDocumentInformatieObject() = DocumentInformatieObject(
        url = URI("http://localhost/informatieobjecttype/0e757153-fce3-44bc-a47f-494840784a16"),
        bronorganisatie = Rsin("404797441"),
        auteur = "y",
        beginRegistratie = LocalDateTime.now(),
        creatiedatum = LocalDate.now(),
        taal = "nl",
        titel = "titel",
        versie = 1,
        informatieobjecttype = "http://localhost/informatieobjecttype",
    )
}