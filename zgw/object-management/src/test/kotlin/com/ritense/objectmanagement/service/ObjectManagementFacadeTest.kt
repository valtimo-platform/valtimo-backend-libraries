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

package com.ritense.objectmanagement.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.client.ObjectsList
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.service.PluginService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import java.net.URI
import java.time.LocalDate
import java.util.UUID

internal class ObjectManagementFacadeTest {
    val objectManagementRepository = mock<ObjectManagementRepository>()
    val pluginService = mock<PluginService>()

    val objectManagementFacade = ObjectManagementFacade(objectManagementRepository, pluginService)

    lateinit var objectTypeId: String
    lateinit var objectManagementTitle: String
    lateinit var objectenApiPluginConfigurationId: UUID
    lateinit var objecttypenApiPluginConfigurationId: UUID
    val objectTypeVersion = 1

    @BeforeEach
    fun setup() {
        objectTypeId = "objectTypeId"
        objectManagementTitle = "myTitle"
        objectenApiPluginConfigurationId = UUID.randomUUID()
        objecttypenApiPluginConfigurationId = UUID.randomUUID()
    }

    @Test
    fun shouldGetObjectByUuid() {
        val objectName = "myObject"
        val objectUuid = UUID.randomUUID()

        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()
        prepareAccessObject(objectName, objectenApiPlugin, objecttypenApiPlugin)

        whenever(objectenApiPlugin.url).thenReturn(URI.create("www.ritense.com/"))
        val expectedUrl = URI.create("www.ritense.com/objects/$objectUuid")
        val expectedResult = createObjectWrapper(url = expectedUrl, uuid = objectUuid)
        whenever(objectenApiPlugin.getObject(expectedUrl)).thenReturn(expectedResult)

        val result = objectManagementFacade.getObjectByUuid(objectName, objectUuid)

        verify(objectManagementRepository).findByTitle(objectName)
        verify(pluginService).createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId)
        verify(pluginService).createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId)
        verifyNoMoreInteractions(objectManagementRepository, pluginService)

        verify(objectenApiPlugin).getObject(expectedUrl)
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun shouldGetObjectsByUuids() {
        val objectName = "myObject"
        val objectUuid1 = UUID.randomUUID()
        val objectUuid2 = UUID.randomUUID()

        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()
        prepareAccessObject(objectName, objectenApiPlugin, objecttypenApiPlugin)

        whenever(objectenApiPlugin.url).thenReturn(URI.create("www.ritense.com/"))
        val expectedUrl1 = URI.create("www.ritense.com/objects/$objectUuid1")
        val expectedUrl2 = URI.create("www.ritense.com/objects/$objectUuid2")
        val expectedResult1 = createObjectWrapper(url = expectedUrl1, uuid = objectUuid1)
        val expectedResult2 = createObjectWrapper(url = expectedUrl2, uuid = objectUuid2)
        whenever(objectenApiPlugin.getObject(expectedUrl1)).thenReturn(expectedResult1)
        whenever(objectenApiPlugin.getObject(expectedUrl2)).thenReturn(expectedResult2)

        val result = objectManagementFacade.getObjectsByUuids(objectName, listOf(objectUuid1, objectUuid2))

        verify(objectManagementRepository).findByTitle(objectName)
        verify(pluginService).createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId)
        verify(pluginService).createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId)
        verifyNoMoreInteractions(objectManagementRepository, pluginService)

        verify(objectenApiPlugin).getObject(expectedUrl1)
        verify(objectenApiPlugin).getObject(expectedUrl2)
        assertThat(result.results.size).isEqualTo(2)
        assertThat(result.results).contains(expectedResult1, expectedResult2)
    }

    @Test
    fun shouldGetObjectByUri() {
        val objectName = "myObject"
        val objectUri = URI.create("www.ritense.com/")

        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()
        prepareAccessObject(objectName, objectenApiPlugin, objecttypenApiPlugin)

        val expectedResult = createObjectWrapper(url = objectUri, uuid = UUID.randomUUID())
        whenever(objectenApiPlugin.getObject(objectUri)).thenReturn(expectedResult)

        val result = objectManagementFacade.getObjectByUri(objectName, objectUri)

        verify(objectManagementRepository).findByTitle(objectName)
        verify(pluginService).createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId)
        verify(pluginService).createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId)
        verifyNoMoreInteractions(objectManagementRepository, pluginService)

        verify(objectenApiPlugin).getObject(objectUri)
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun shouldGetObjectsByUris() {
        val objectName = "myObject"
        val objectUri1 = URI.create("www.ritense.com/1")
        val objectUri2 = URI.create("www.ritense.com/2")

        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()
        prepareAccessObject(objectName, objectenApiPlugin, objecttypenApiPlugin)

        val expectedResult1 = createObjectWrapper(url = objectUri1, uuid = UUID.randomUUID())
        val expectedResult2 = createObjectWrapper(url = objectUri2, uuid = UUID.randomUUID())
        whenever(objectenApiPlugin.getObject(objectUri1)).thenReturn(expectedResult1)
        whenever(objectenApiPlugin.getObject(objectUri2)).thenReturn(expectedResult2)

        val result = objectManagementFacade.getObjectsByUris(objectName, listOf(objectUri1, objectUri2))

        verify(objectManagementRepository).findByTitle(objectName)
        verify(pluginService).createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId)
        verify(pluginService).createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId)
        verifyNoMoreInteractions(objectManagementRepository, pluginService)

        verify(objectenApiPlugin).getObject(objectUri1)
        verify(objectenApiPlugin).getObject(objectUri2)
        assertThat(result.results.size).isEqualTo(2)
        assertThat(result.results).contains(expectedResult1, expectedResult2)
    }

    @Test
    fun shouldGetObjectsPagedWithSearchString() {
        val objectName = "myObject"
        val searchString = "mySearchString"
        val pageNumber = 1
        val pageSize = 20

        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()
        prepareAccessObject(objectName, objectenApiPlugin, objecttypenApiPlugin)

        val expectedPageRequest = PageRequest.of(pageNumber, pageSize)
        val expectedUrl = URI.create("www.ritense.com/")
        val expectedObjectWrapper = createObjectWrapper(url = expectedUrl, uuid = UUID.randomUUID())
        val expectedObjectsList = ObjectsList(count = 1, results = listOf(expectedObjectWrapper))
        whenever(objecttypenApiPlugin.url).thenReturn(expectedUrl)
        whenever(objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = expectedUrl,
            objecttypeId = objectTypeId,
            searchString = searchString,
            pageable = expectedPageRequest)
        ).thenReturn(expectedObjectsList)

        val result = objectManagementFacade.getObjectsPaged(objectName, searchString, pageNumber, pageSize)

        verify(objectManagementRepository).findByTitle(objectName)
        verify(pluginService).createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId)
        verify(pluginService).createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId)
        verifyNoMoreInteractions(objectManagementRepository, pluginService)

        verify(objectenApiPlugin).getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = expectedUrl,
            objecttypeId = objectTypeId,
            searchString = searchString,
            pageable = expectedPageRequest)
        verify(objectenApiPlugin, never()).getObjectsByObjectTypeId(any(), any(), any(), any())
        assertThat(result).isEqualTo(expectedObjectsList)
    }

    @Test
    fun shouldGetObjectsPagedWithoutSearchString() {
        val objectName = "myObject"
        val searchString = null
        val pageNumber = 1
        val pageSize = 20

        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()
        prepareAccessObject(objectName, objectenApiPlugin, objecttypenApiPlugin)

        val expectedPageRequest = PageRequest.of(pageNumber, pageSize)
        val expectedUrl1 = URI.create("www.ritense.com/1")
        val expectedUrl2 = URI.create("www.ritense.com/2")
        val expectedObjectWrapper = createObjectWrapper(url = expectedUrl1, uuid = UUID.randomUUID())
        val expectedObjectsList = ObjectsList(count = 1, results = listOf(expectedObjectWrapper))
        whenever(objecttypenApiPlugin.url).thenReturn(expectedUrl1)
        whenever(objectenApiPlugin.url).thenReturn(expectedUrl2)
        whenever(objectenApiPlugin.getObjectsByObjectTypeId(
            objecttypesApiUrl = expectedUrl1,
            objectsApiUrl = expectedUrl2,
            objecttypeId = objectTypeId,
            pageable = expectedPageRequest)
        ).thenReturn(expectedObjectsList)

        val result = objectManagementFacade.getObjectsPaged(objectName, searchString, pageNumber, pageSize)

        verify(objectManagementRepository).findByTitle(objectName)
        verify(pluginService).createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId)
        verify(pluginService).createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId)
        verifyNoMoreInteractions(objectManagementRepository, pluginService)

        verify(objectenApiPlugin).getObjectsByObjectTypeId(
            objecttypesApiUrl = expectedUrl1,
            objectsApiUrl = expectedUrl2,
            objecttypeId = objectTypeId,
            pageable = expectedPageRequest)
        verify(objectenApiPlugin, never()).getObjectsByObjectTypeIdWithSearchParams(any(), any(), any(), any())
        assertThat(result).isEqualTo(expectedObjectsList)
    }

    @Test
    fun shouldGetObjectsUnpagedWithSearchString() {
        val objectName = "myObject"
        val searchString = "mySearchString"

        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()
        prepareAccessObject(objectName, objectenApiPlugin, objecttypenApiPlugin)

        val expectedPageRequest = PageRequest.of(0, 500)
        val expectedUrl = URI.create("www.ritense.com/")
        val expectedObjectWrapper = createObjectWrapper(url = expectedUrl, uuid = UUID.randomUUID())
        val expectedObjectsList = ObjectsList(count = 1, results = listOf(expectedObjectWrapper))
        whenever(objecttypenApiPlugin.url).thenReturn(expectedUrl)
        whenever(objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = expectedUrl,
            objecttypeId = objectTypeId,
            searchString = searchString,
            pageable = expectedPageRequest)
        ).thenReturn(expectedObjectsList)

        val result = objectManagementFacade.getObjectsUnpaged(objectName, searchString)

        verify(objectManagementRepository).findByTitle(objectName)
        verify(pluginService).createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId)
        verify(pluginService).createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId)
        verifyNoMoreInteractions(objectManagementRepository, pluginService)

        verify(objectenApiPlugin).getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = expectedUrl,
            objecttypeId = objectTypeId,
            searchString = searchString,
            pageable = expectedPageRequest)
        verify(objectenApiPlugin, never()).getObjectsByObjectTypeId(any(), any(), any(), any())
        assertThat(result.count).isEqualTo(expectedObjectsList.count)
        assertThat(result.next).isEqualTo(expectedObjectsList.next)
        assertThat(result.previous).isEqualTo(expectedObjectsList.previous)
        assertThat(result.results).contains(expectedObjectWrapper)
    }

    @Test
    fun shouldGetObjectsUnpagedWithoutSearchString() {
        val objectName = "myObject"
        val searchString = null

        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()
        prepareAccessObject(objectName, objectenApiPlugin, objecttypenApiPlugin)

        val expectedPageRequest = PageRequest.of(0, 500)
        val expectedUrl1 = URI.create("www.ritense.com/1")
        val expectedUrl2 = URI.create("www.ritense.com/2")
        val expectedObjectWrapper = createObjectWrapper(url = expectedUrl1, uuid = UUID.randomUUID())
        val expectedObjectsList = ObjectsList(count = 1, results = listOf(expectedObjectWrapper))
        whenever(objecttypenApiPlugin.url).thenReturn(expectedUrl1)
        whenever(objectenApiPlugin.url).thenReturn(expectedUrl2)
        whenever(objectenApiPlugin.getObjectsByObjectTypeId(
            objecttypesApiUrl = expectedUrl1,
            objectsApiUrl = expectedUrl2,
            objecttypeId = objectTypeId,
            pageable = expectedPageRequest)
        ).thenReturn(expectedObjectsList)

        val result = objectManagementFacade.getObjectsUnpaged(objectName, searchString)

        verify(objectManagementRepository).findByTitle(objectName)
        verify(pluginService).createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId)
        verify(pluginService).createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId)
        verifyNoMoreInteractions(objectManagementRepository, pluginService)

        verify(objectenApiPlugin).getObjectsByObjectTypeId(
            objecttypesApiUrl = expectedUrl1,
            objectsApiUrl = expectedUrl2,
            objecttypeId = objectTypeId,
            pageable = expectedPageRequest)
        verify(objectenApiPlugin, never()).getObjectsByObjectTypeIdWithSearchParams(any(), any(), any(), any())
        assertThat(result.count).isEqualTo(expectedObjectsList.count)
        assertThat(result.next).isEqualTo(expectedObjectsList.next)
        assertThat(result.previous).isEqualTo(expectedObjectsList.previous)
        assertThat(result.results).contains(expectedObjectWrapper)
    }

    @Test
    fun shouldCreateObject() {
        val objectName = "myObject"
        val data: JsonNode = JsonNodeFactory(false).objectNode()

        val objectenApiPlugin = mock<ObjectenApiPlugin>()
        val objecttypenApiPlugin = mock<ObjecttypenApiPlugin>()
        prepareAccessObject(objectName, objectenApiPlugin, objecttypenApiPlugin)

        val expectedUrl = URI.create("www.ritense.com")
        whenever(objecttypenApiPlugin.getObjectTypeUrlById(objectTypeId)).thenReturn(expectedUrl)

        val objectRecord = ObjectRecord(
        typeVersion = objectTypeVersion,
        data = data,
        startAt = LocalDate.now()
        )

        val expectedObjectRequest = ObjectRequest(expectedUrl, objectRecord)

        objectManagementFacade.createObject(objectName, data)

        verify(objectManagementRepository).findByTitle(objectName)
        verify(pluginService).createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId)
        verify(pluginService).createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId)
        verifyNoMoreInteractions(objectManagementRepository, pluginService)

        val objectRequestCaptor = argumentCaptor<ObjectRequest>()
        verify(objectenApiPlugin).createObject(objectRequestCaptor.capture())
        val actualObjectRequest = objectRequestCaptor.firstValue
        assertThat(actualObjectRequest.type).isEqualTo(expectedObjectRequest.type)
        val actualObjectRecord = actualObjectRequest.record
        assertThat(actualObjectRecord.typeVersion).isEqualTo(objectTypeVersion)
        assertThat(actualObjectRecord.data).isEqualTo(data)
        assertThat(actualObjectRecord.startAt).isEqualTo(LocalDate.now())
    }

    private fun prepareAccessObject(
        objectName: String,
        objectenApiPlugin: ObjectenApiPlugin,
        objecttypenApiPlugin: ObjecttypenApiPlugin
    ) {
        val objectManagement = ObjectManagement(
            objecttypeId = objectTypeId,
            title = objectManagementTitle,
            objectenApiPluginConfigurationId = objectenApiPluginConfigurationId,
            objecttypenApiPluginConfigurationId = objecttypenApiPluginConfigurationId
        )

        whenever(objectManagementRepository.findByTitle(objectName)).thenReturn(objectManagement)
        whenever(pluginService.createInstance<ObjectenApiPlugin>(objectenApiPluginConfigurationId))
            .thenReturn(objectenApiPlugin)
        whenever(pluginService.createInstance<ObjecttypenApiPlugin>(objecttypenApiPluginConfigurationId))
            .thenReturn(objecttypenApiPlugin)
    }

    private fun createObjectManagement(): ObjectManagement = ObjectManagement(
        objecttypeId = objectTypeId,
        title = objectManagementTitle,
        objectenApiPluginConfigurationId = objectenApiPluginConfigurationId,
        objecttypenApiPluginConfigurationId = objecttypenApiPluginConfigurationId,
        objecttypeVersion = objectTypeVersion
    )

    private fun createObjectWrapper(url: URI, uuid: UUID): ObjectWrapper = ObjectWrapper(
        record = ObjectRecord(startAt = LocalDate.now(), typeVersion = 1),
        type = URI.create("myURL"),
        url = url,
        uuid = uuid
    )
}