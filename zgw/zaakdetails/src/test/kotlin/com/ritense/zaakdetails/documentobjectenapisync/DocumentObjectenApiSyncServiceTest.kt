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

package com.ritense.zaakdetails.documentobjectenapisync

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.domain.Document
import com.ritense.document.domain.DocumentContent
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.domain.event.DocumentCreatedEvent
import com.ritense.document.service.DocumentService
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.client.ObjectsList
import com.ritense.objectenapi.management.ObjectManagementInfo
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.service.PluginService
import com.ritense.zaakdetails.domain.ZaakdetailsObject
import com.ritense.zaakdetails.service.ZaakdetailsObjectService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.link.ZaakInstanceLinkNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.*

internal class DocumentObjectenApiSyncServiceTest {
    lateinit var service: DocumentObjectenApiSyncService
    lateinit var documentObjectenApiSyncRepository: DocumentObjectenApiSyncRepository
    lateinit var objectObjectManagementInfoProvider: ObjectManagementInfoProvider
    lateinit var documentService: DocumentService
    lateinit var pluginService: PluginService
    lateinit var objectSyncService: ObjectSyncService
    lateinit var zaakUrlProvider: ZaakUrlProvider
    lateinit var zaakdetailsObjectService: ZaakdetailsObjectService
    lateinit var documentObjectenApiSyncManagementService: DocumentObjectenApiSyncManagementService
    lateinit var objectMapper: ObjectMapper

    lateinit var objectenApiPlugin: ObjectenApiPlugin
    lateinit var objecttypenApiPlugin: ObjecttypenApiPlugin
    lateinit var zakenApiPlugin: ZakenApiPlugin

    lateinit var document: Document
    lateinit var documentCreatedEvent: DocumentCreatedEvent

    @BeforeEach
    fun init() {
        documentObjectenApiSyncRepository = mock()
        objectObjectManagementInfoProvider = mock()
        documentService = mock()
        pluginService = mock()
        objectSyncService = mock()
        zaakUrlProvider = mock()
        zaakdetailsObjectService = mock()
        documentObjectenApiSyncManagementService = mock()
        service = DocumentObjectenApiSyncService(
            objectObjectManagementInfoProvider = objectObjectManagementInfoProvider,
            documentService = documentService,
            pluginService = pluginService,
            zaakUrlProvider = zaakUrlProvider,
            zaakdetailsObjectService = zaakdetailsObjectService,
            documentObjectenApiSyncManagementService = documentObjectenApiSyncManagementService,
            linkZaakdetailsToZaakEnabled = true
        )
        objectMapper = ObjectMapper()
        objectenApiPlugin = mock()
        objecttypenApiPlugin = mock()
        zakenApiPlugin = mock()

        val objectManagementInfo = setupObjectManagementInfo()
        whenever(pluginService.createInstance<ObjectenApiPlugin>(objectManagementInfo.objectenApiPluginConfigurationId))
            .thenReturn(objectenApiPlugin)

        whenever(pluginService.createInstance<ObjecttypenApiPlugin>(objectManagementInfo.objecttypenApiPluginConfigurationId))
            .thenReturn(objecttypenApiPlugin)

        document = setupDocument()
        documentCreatedEvent = setupDocumentCreatedEvent()
    }

    @Test
    fun `should not sync when sync is not enabled`() {
        whenever(documentObjectenApiSyncManagementService.getSyncConfiguration(any(), any()))
            .thenReturn(null)

        service.handleDocumentCreatedEvent(documentCreatedEvent)
        verify(objectenApiPlugin, never()).createObject(any())
    }

    @Test
    fun `should create object and link to zaak when reference does not exist and object does not exist`() {
        val documentenApiSync = setupDocumentObjectenApiSync()
        val objectWrapper = ObjectWrapper(
            url = URI.create("http://localhost/"),
            uuid = UUID.randomUUID(),
            type = URI.create("http://localhost/"),
            record = mock<ObjectRecord>()
        )

        whenever(documentObjectenApiSyncManagementService.getSyncConfiguration(any(), any()))
            .thenReturn(documentenApiSync)

        whenever(objecttypenApiPlugin.getObjectTypeUrlById(any()))
            .thenReturn(URI.create("http://localhost/"))

        whenever(zaakdetailsObjectService.findByDocumentId(any()))
            .thenReturn(Optional.empty())

        whenever(objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = anyOrNull(),
            objecttypeId = any(),
            searchString = any(),
            pageable = anyOrNull(),
            ordering = anyOrNull()
        ))
            .thenReturn(ObjectsList(0, null, null, listOf()))

        whenever(objectenApiPlugin.createObject(any()))
            .thenReturn(objectWrapper)

        whenever(zaakUrlProvider.getZaakUrl(any()))
            .thenReturn(URI.create("http://localhost/"))

        whenever(pluginService.createInstance<ZakenApiPlugin>(any(), any()))
            .thenReturn(zakenApiPlugin)

        whenever(zakenApiPlugin.getZaakObject(any(), any()))
            .thenReturn(null)

        service.handleDocumentCreatedEvent(documentCreatedEvent)
        verify(objectenApiPlugin, times(1)).createObject(any())
        verify(zakenApiPlugin, times(1)).createZaakObject(any(), any(), any(), any())
    }

    @Test
    fun `should update object when reference exists`() {
        val documentenApiSync = setupDocumentObjectenApiSync()
        val objectWrapper = ObjectWrapper(
            url = URI.create("http://localhost/"),
            uuid = UUID.randomUUID(),
            type = URI.create("http://localhost/"),
            record = mock<ObjectRecord>()
        )

        whenever(documentObjectenApiSyncManagementService.getSyncConfiguration(any(), any()))
            .thenReturn(documentenApiSync)

        whenever(objecttypenApiPlugin.getObjectTypeUrlById(any()))
            .thenReturn(URI.create("http://localhost/"))

        whenever(zaakdetailsObjectService.findByDocumentId(any()))
            .thenReturn(Optional.of(ZaakdetailsObject(
                documentId = UUID.randomUUID(),
                URI.create("http://localhost/"),
                linkedToZaak = true
            )))

        whenever(objectenApiPlugin.objectUpdate(any(), any()))
            .thenReturn(objectWrapper)

        service.handleDocumentCreatedEvent(documentCreatedEvent)
        verify(objectenApiPlugin, times(1)).objectUpdate(any(), any())
        verifyNoMoreInteractions(objectenApiPlugin)
        verifyNoInteractions(zakenApiPlugin)
    }

    @Test
    fun `should update object when reference does not exist and object exists`() {
        val documentenApiSync = setupDocumentObjectenApiSync()
        val objectWrapper = ObjectWrapper(
            url = URI.create("http://localhost/"),
            uuid = UUID.randomUUID(),
            type = URI.create("http://localhost/"),
            record = mock<ObjectRecord>()
        )

        whenever(documentObjectenApiSyncManagementService.getSyncConfiguration(any(), any()))
            .thenReturn(documentenApiSync)

        whenever(objecttypenApiPlugin.getObjectTypeUrlById(any()))
            .thenReturn(URI.create("http://localhost/"))

        whenever(zaakdetailsObjectService.findByDocumentId(any()))
            .thenReturn(Optional.empty())

        whenever(objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = anyOrNull(),
            objecttypeId = any(),
            searchString = any(),
            pageable = anyOrNull(),
            ordering = anyOrNull()
        ))
            .thenReturn(ObjectsList(1, null, null, listOf(objectWrapper)))

        whenever(objectenApiPlugin.objectUpdate(any(), any()))
            .thenReturn(objectWrapper)

        whenever(zaakUrlProvider.getZaakUrl(any()))
            .thenReturn(URI.create("http://localhost/"))

        whenever(pluginService.createInstance<ZakenApiPlugin>(any(), any()))
            .thenReturn(zakenApiPlugin)

        service.handleDocumentCreatedEvent(documentCreatedEvent)
        verify(objectenApiPlugin, times(1)).getObjectsByObjectTypeIdWithSearchParams(
            anyOrNull(), any(), any(), anyOrNull(), anyOrNull()
        )
        verify(objectenApiPlugin, times(1)).objectUpdate(any(), any())
        verifyNoMoreInteractions(objectenApiPlugin)
        verify(zaakdetailsObjectService, times(2)).save(any())
        verify(zakenApiPlugin, times(1)).getZaakObject(any(), any())
        verify(zakenApiPlugin, times(1)).createZaakObject(any(), any(), any(), any())
        verifyNoMoreInteractions(zakenApiPlugin)
    }

    @Test
    fun `should not link to zaak when zaak does not exist`() {
        val documentenApiSync = setupDocumentObjectenApiSync()
        val objectWrapper = ObjectWrapper(
            url = URI.create("http://localhost/"),
            uuid = UUID.randomUUID(),
            type = URI.create("http://localhost/"),
            record = mock<ObjectRecord>()
        )

        whenever(documentObjectenApiSyncManagementService.getSyncConfiguration(any(), any()))
            .thenReturn(documentenApiSync)

        whenever(objecttypenApiPlugin.getObjectTypeUrlById(any()))
            .thenReturn(URI.create("http://localhost/"))

        whenever(zaakdetailsObjectService.findByDocumentId(any()))
            .thenReturn(Optional.empty())

        whenever(objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = anyOrNull(),
            objecttypeId = any(),
            searchString = any(),
            pageable = anyOrNull(),
            ordering = anyOrNull()
        ))
            .thenReturn(ObjectsList(0, null, null, listOf()))

        whenever(objectenApiPlugin.createObject(any()))
            .thenReturn(objectWrapper)

        whenever(zaakUrlProvider.getZaakUrl(any()))
            .thenThrow(ZaakInstanceLinkNotFoundException("No ZaakInstanceLink found for document"))

        service.handleDocumentCreatedEvent(documentCreatedEvent)
        verify(objectenApiPlugin, times(1)).createObject(any())
        verifyNoInteractions(zakenApiPlugin)
    }

    @Test
    fun `should not link to zaak when plugin is not configured`() {
        val documentenApiSync = setupDocumentObjectenApiSync()
        val objectWrapper = ObjectWrapper(
            url = URI.create("http://localhost/"),
            uuid = UUID.randomUUID(),
            type = URI.create("http://localhost/"),
            record = mock<ObjectRecord>()
        )

        whenever(documentObjectenApiSyncManagementService.getSyncConfiguration(any(), any()))
            .thenReturn(documentenApiSync)

        whenever(objecttypenApiPlugin.getObjectTypeUrlById(any()))
            .thenReturn(URI.create("http://localhost/"))

        whenever(zaakdetailsObjectService.findByDocumentId(any()))
            .thenReturn(Optional.empty())

        whenever(objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = anyOrNull(),
            objecttypeId = any(),
            searchString = any(),
            pageable = anyOrNull(),
            ordering = anyOrNull()
        ))
            .thenReturn(ObjectsList(0, null, null, listOf()))

        whenever(objectenApiPlugin.createObject(any()))
            .thenReturn(objectWrapper)

        whenever(zaakUrlProvider.getZaakUrl(any()))
            .thenReturn(URI.create("http://localhost/"))

        whenever(pluginService.createInstance<ZakenApiPlugin>(any(), any()))
            .thenReturn(null)

        service.handleDocumentCreatedEvent(documentCreatedEvent)
        verify(objectenApiPlugin, times(1)).createObject(any())
        verifyNoInteractions(zakenApiPlugin)
    }

    @Test
    fun `should not link to zaak when linking is disabled`() {
        DocumentObjectenApiSyncService(
            objectObjectManagementInfoProvider = objectObjectManagementInfoProvider,
            documentService = documentService,
            pluginService = pluginService,
            zaakUrlProvider = zaakUrlProvider,
            zaakdetailsObjectService = zaakdetailsObjectService,
            documentObjectenApiSyncManagementService = documentObjectenApiSyncManagementService,
            linkZaakdetailsToZaakEnabled = false
        )

        val documentenApiSync = setupDocumentObjectenApiSync()
        val objectWrapper = ObjectWrapper(
            url = URI.create("http://localhost/"),
            uuid = UUID.randomUUID(),
            type = URI.create("http://localhost/"),
            record = mock<ObjectRecord>()
        )

        whenever(documentObjectenApiSyncManagementService.getSyncConfiguration(any(), any()))
            .thenReturn(documentenApiSync)

        whenever(objecttypenApiPlugin.getObjectTypeUrlById(any()))
            .thenReturn(URI.create("http://localhost/"))

        whenever(zaakdetailsObjectService.findByDocumentId(any()))
            .thenReturn(Optional.empty())

        whenever(objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = anyOrNull(),
            objecttypeId = any(),
            searchString = any(),
            pageable = anyOrNull(),
            ordering = anyOrNull()
        ))
            .thenReturn(ObjectsList(0, null, null, listOf()))

        whenever(objectenApiPlugin.createObject(any()))
            .thenReturn(objectWrapper)

        verifyNoInteractions(zaakdetailsObjectService)
    }

    private fun setupDocumentCreatedEvent(): DocumentCreatedEvent {
        val event = mock<DocumentCreatedEvent>()
        val idMock = mock<DocumentDefinition.Id>()
        val documentId = mock<Document.Id>()
        val documentUUID = UUID.randomUUID()
        whenever(event.definitionId()).thenReturn(idMock)
        whenever(idMock.name()).thenReturn("test")
        whenever(event.documentId()).thenReturn(documentId)
        whenever(documentId.id).thenReturn(documentUUID)
        return event
    }

    private fun setupDocument(): Document {
        val document = mock<Document>()
        val documentDefinitionId = mock<DocumentDefinition.Id>()
        val documentId = mock<Document.Id>()
        val documentContent = mock<DocumentContent>()
        whenever(document.definitionId()).thenReturn(documentDefinitionId)
        whenever(documentDefinitionId.name()).thenReturn("test")

        whenever(document.id()).thenReturn(documentId)
        whenever(documentId.id).thenReturn(UUID.randomUUID())

        whenever(documentService.get(any())).thenReturn(document)

        whenever(document.content()).thenReturn(documentContent)
        whenever(documentContent.asJson()).thenReturn(objectMapper.createObjectNode())

        return document
    }

    private fun setupDocumentObjectenApiSync(): DocumentObjectenApiSync {
        val documentObjectenApiSync = DocumentObjectenApiSync(
            documentDefinitionName = "test",
            documentDefinitionVersion = 1L,
            objectManagementConfigurationId = UUID.randomUUID(),
            enabled = true
        )

        return documentObjectenApiSync
    }

    private fun setupObjectManagementInfo(): ObjectManagementInfo {
        val objectManagementInfo = mock<ObjectManagementInfo>()
        whenever(objectManagementInfo.objectenApiPluginConfigurationId).thenReturn(UUID.randomUUID())
        whenever(objectManagementInfo.objecttypenApiPluginConfigurationId).thenReturn(UUID.randomUUID())
        whenever(objectManagementInfo.objecttypeVersion).thenReturn(1)
        whenever(objectManagementInfo.objecttypeId).thenReturn("test")

        whenever(objectObjectManagementInfoProvider.getObjectManagementInfo(any())).thenReturn(objectManagementInfo)

        return objectManagementInfo
    }
}