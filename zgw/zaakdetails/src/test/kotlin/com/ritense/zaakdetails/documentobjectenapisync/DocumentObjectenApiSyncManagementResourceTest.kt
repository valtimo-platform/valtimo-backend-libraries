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
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.zaakdetails.mock.MockObjectManagement
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
internal class DocumentObjectenApiSyncManagementResourceTest {

    private var objectMapper: ObjectMapper = MapperSingleton.get()

    lateinit var mockMvc: MockMvc
    lateinit var resource: DocumentObjectenApiSyncManagementResource
    lateinit var service: DocumentObjectenApiSyncManagementService
    lateinit var objectManagementInfoProvider: ObjectManagementInfoProvider

    @BeforeEach
    fun init() {
        service = mock()
        objectManagementInfoProvider = mock()
        resource = DocumentObjectenApiSyncManagementResource(service, objectManagementInfoProvider)
        mockMvc = MockMvcBuilders.standaloneSetup(resource).build()
    }

    @Test
    fun `should get sync configuration`() {
        val objectManagementConfigurationId = UUID.randomUUID()
        whenever(service.getSyncConfiguration("test-case", 1)).thenReturn(
            DocumentObjectenApiSync(
                documentDefinitionName = "test-case",
                documentDefinitionVersion = 1,
                objectManagementConfigurationId = objectManagementConfigurationId,
            )
        )
        whenever(objectManagementInfoProvider.getObjectManagementInfo(objectManagementConfigurationId)).thenReturn(
            MockObjectManagement(
                title = "test-object",
                objecttypeId = "test-objecttypeId"
            )
        )
        mockMvc.perform(
            get("/api/management/v1/document-definition/{name}/version/{version}/objecten-api-sync", "test-case", 1)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.objectManagementConfigurationId").value(objectManagementConfigurationId.toString()))
            .andExpect(jsonPath("$.objectManagementConfigurationTitle").value("test-object"))
            .andExpect(jsonPath("$.objectManagementConfigurationObjecttypeVersion").value(1))
            .andExpect(jsonPath("$.enabled").value(true))
    }

    @Test
    fun `should save sync configuration`() {
        val request = DocumentObjectenApiSyncRequest(
            objectManagementConfigurationId = UUID.randomUUID(),
            enabled = true,
        )
        mockMvc.perform(
            put("/api/management/v1/document-definition/{name}/version/{version}/objecten-api-sync", "test-case", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk())

        verify(service, times(1)).saveSyncConfiguration(any())
    }

    @Test
    fun `should delete sync configuration`() {
        mockMvc.perform(
            delete("/api/management/v1/document-definition/{name}/version/{version}/objecten-api-sync", "test-case", 1)
        )
            .andDo(print())
            .andExpect(status().isOk())

        verify(service, times(1)).deleteSyncConfigurationByDocumentDefinition("test-case", 1)
    }
}
