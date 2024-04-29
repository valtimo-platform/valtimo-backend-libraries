package com.ritense.documentenapi.service

import com.ritense.authorization.AuthorizationService
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.documentenapi.web.rest.dto.RelatedFileDto
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcessLink
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcessLinkId
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.processlink.service.PluginProcessLinkService
import com.ritense.zgw.Rsin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class DocumentenApiServiceTest {
    private lateinit var service: DocumentenApiService

    private lateinit var pluginService: PluginService
    private lateinit var catalogiService: CatalogiService
    private lateinit var documentenApiColumnRepository: DocumentenApiColumnRepository
    private lateinit var authorizationService: AuthorizationService
    private lateinit var valtimoDocumentService: DocumentService
    private lateinit var documentDefinitionService: JsonSchemaDocumentDefinitionService
    private lateinit var documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService
    private lateinit var pluginProcessLinkService: PluginProcessLinkService
    private lateinit var camundaRepositoryService: CamundaRepositoryService

    @BeforeEach
    fun before() {
        pluginService = mock<PluginService>()
        catalogiService = mock<CatalogiService>()
        documentenApiColumnRepository = mock<DocumentenApiColumnRepository>()
        authorizationService = mock<AuthorizationService>()
        valtimoDocumentService = mock<DocumentService>()
        documentDefinitionService = mock<JsonSchemaDocumentDefinitionService>()
        documentDefinitionProcessLinkService = mock<DocumentDefinitionProcessLinkService>()
        pluginProcessLinkService = mock<PluginProcessLinkService>()
        camundaRepositoryService = mock<CamundaRepositoryService>()

        service = DocumentenApiService(
            pluginService,
            catalogiService,
            documentenApiColumnRepository,
            authorizationService,
            valtimoDocumentService,
            documentDefinitionService,
            documentDefinitionProcessLinkService,
            pluginProcessLinkService,
            camundaRepositoryService
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
        val processLink = mock<DocumentDefinitionProcessLink>()
        whenever(documentDefinitionProcessLinkService.getDocumentDefinitionProcessLink(any(), any())).thenReturn(
            Optional.of(processLink))
        val processLinkId = DocumentDefinitionProcessLinkId.existingId("some-document-name", "some-process-name")
        whenever(processLink.id).thenReturn(processLinkId)
        val processDefinition = mock<CamundaProcessDefinition>()
        whenever(camundaRepositoryService.findLinkedProcessDefinitions(any())).thenReturn(listOf(processDefinition))
        whenever(processDefinition.id).thenReturn("some-process-id")
        val pluginProcessLink = mock<PluginProcessLink>()
        whenever(pluginProcessLinkService.getProcessLinks("some-process-id")).thenReturn(listOf(pluginProcessLink))
        val pluginConfigurationId = PluginConfigurationId.existingId(UUID.randomUUID())
        whenever(pluginProcessLink.pluginConfigurationId).thenReturn(pluginConfigurationId)
        val pluginConfiguration = mock<PluginConfiguration>()
        whenever(pluginService.getPluginConfiguration(pluginConfigurationId)).thenReturn(pluginConfiguration)
        val pluginDefinition = mock<PluginDefinition>()
        whenever(pluginDefinition.key).thenReturn(DocumentenApiPlugin.PLUGIN_KEY)
        whenever(pluginConfiguration.pluginDefinition).thenReturn(pluginDefinition)
        whenever(pluginConfiguration.id).thenReturn(pluginConfigurationId)
        val plugin = mock<DocumentenApiPlugin>()
        whenever(pluginService.createInstance<DocumentenApiPlugin>(any<UUID>())).thenReturn(plugin)
        val relatedFile = createDocumentInformatieObject()
        val page = PageImpl(listOf(relatedFile), pageable, 1)
        whenever(plugin.getInformatieObjecten(documentSearchRequest, pageable)).thenReturn(page)
        val informatieobjecttype = mock<Informatieobjecttype>()
        whenever(catalogiService.getInformatieobjecttype(URI("http://localhost/informatieobjecttype")))
            .thenReturn(informatieobjecttype)
        whenever(informatieobjecttype.omschrijving).thenReturn("informatieobjecttype")

        val resultPage = service.getCaseInformatieObjecten(documentId, documentSearchRequest, pageable)

        assertEquals(1, resultPage.size)
        val firstResult = resultPage.content.first()
        assertInstanceOf(RelatedFileDto::class.java, firstResult)
        val relatedFileDto = firstResult as RelatedFileDto
        assertEquals("nl", firstResult.language)
        assertEquals("titel", firstResult.title)
        assertEquals(1, firstResult.version)
        assertEquals("informatieobjecttype", firstResult.informatieobjecttype)
        assertEquals("y", firstResult.author)
        assertEquals(UUID.fromString("0e757153-fce3-44bc-a47f-494840784a16"), firstResult.fileId)
    }

    @Test
    fun `getCaseInformatieObjecten should throw exception when multiple plugins are found`() {
        val documentId = UUID.randomUUID()
        val documentSearchRequest = DocumentSearchRequest()
        val pageable = Pageable.unpaged()

        val documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId("some-document-name", 3)
        val document = mock<Document>()
        whenever(valtimoDocumentService.get(documentId.toString())).thenReturn(document)
        whenever(document.definitionId()).thenReturn(documentDefinitionId)
        val processLink = mock<DocumentDefinitionProcessLink>()
        whenever(documentDefinitionProcessLinkService.getDocumentDefinitionProcessLink(any(), any())).thenReturn(
            Optional.of(processLink))
        val processLinkId = DocumentDefinitionProcessLinkId.existingId("some-document-name", "some-process-name")
        whenever(processLink.id).thenReturn(processLinkId)
        val processDefinition = mock<CamundaProcessDefinition>()
        val processDefinition2 = mock<CamundaProcessDefinition>()
        whenever(camundaRepositoryService.findLinkedProcessDefinitions(any())).thenReturn(listOf(processDefinition, processDefinition2))
        whenever(processDefinition.id).thenReturn("some-process-id")
        whenever(processDefinition2.id).thenReturn("some-process-id")
        val pluginProcessLink = mock<PluginProcessLink>()
        whenever(pluginProcessLinkService.getProcessLinks("some-process-id")).thenReturn(listOf(pluginProcessLink))
        val pluginConfigurationId = PluginConfigurationId.existingId(UUID.randomUUID())
        whenever(pluginProcessLink.pluginConfigurationId).thenReturn(pluginConfigurationId)
        val pluginConfiguration = mock<PluginConfiguration>()
        whenever(pluginService.getPluginConfiguration(pluginConfigurationId)).thenReturn(pluginConfiguration)
        val pluginDefinition = mock<PluginDefinition>()
        whenever(pluginDefinition.key).thenReturn(DocumentenApiPlugin.PLUGIN_KEY)
        whenever(pluginConfiguration.pluginDefinition).thenReturn(pluginDefinition)
        whenever(pluginConfiguration.id).thenReturn(pluginConfigurationId)

        assertThrows<IllegalStateException> {
            val resultPage = service.getCaseInformatieObjecten(documentId, documentSearchRequest, pageable)
        }
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