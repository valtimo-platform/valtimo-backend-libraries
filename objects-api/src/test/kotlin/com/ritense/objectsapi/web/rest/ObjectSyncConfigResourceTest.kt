/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.web.rest

import com.ritense.objectsapi.BaseTest
import com.ritense.objectsapi.domain.sync.ObjectSyncConfig
import com.ritense.objectsapi.domain.sync.ObjectSyncConfigId
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objectsapi.web.rest.impl.ObjectSyncConfigResource
import com.ritense.objectsapi.web.rest.request.CreateObjectSyncConfigRequest
import com.ritense.objectsapi.web.rest.result.CreateObjectSyncConfigResultSucceeded
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets
import java.util.UUID

internal class ObjectSyncConfigResourceTest : BaseTest() {

    lateinit var mockMvc: MockMvc

    @Mock
    lateinit var objectSyncService: ObjectSyncService
    lateinit var objectSyncConfigResource: ObjectSyncConfigResource

    @BeforeEach
    fun init() {
        super.baseSetUp()
        objectSyncConfigResource = ObjectSyncConfigResource(objectSyncService)
        mockMvc = MockMvcBuilders.standaloneSetup(objectSyncConfigResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()
    }

    @Test
    fun `should get 200 with all configs in body`() {
        `when`(objectSyncService.getObjectSyncConfig("aName")).thenReturn(
            PageImpl(
                listOf(
                    ObjectSyncConfig(
                        ObjectSyncConfigId.newId(UUID.randomUUID()),
                        UUID.randomUUID(),
                        true,
                        "aName",
                        UUID.randomUUID()
                    )
                )
            )
        )

        mockMvc.perform(get("/api/object/sync/config")
            .param("documentDefinitionName", "aName")
            .accept(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `should get 200 with config in body`() {
        val configId = ObjectSyncConfigId.newId(UUID.randomUUID())

        `when`(objectSyncService.getObjectSyncConfig(configId.id)).thenReturn(
            ObjectSyncConfig(
                ObjectSyncConfigId.newId(UUID.randomUUID()),
                UUID.randomUUID(),
                true,
                "aName",
                UUID.randomUUID()
            )
        )

        mockMvc.perform(get("/api/object/sync/config/{id}", configId.id.toString())
            .accept(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
    }

    @Test
    fun `should return 200 with new config in body`() {

        val request = CreateObjectSyncConfigRequest(
            UUID.randomUUID(),
            true,
            "aName",
            UUID.randomUUID()
        )

        `when`(objectSyncService.createObjectSyncConfig(request)).thenReturn(
            CreateObjectSyncConfigResultSucceeded(
                ObjectSyncConfig(
                    ObjectSyncConfigId.newId(UUID.randomUUID()),
                    request.connectorInstanceId,
                    request.enabled,
                    request.documentDefinitionName,
                    request.objectTypeId
                )
            )
        )

        mockMvc.perform(
            post(
                "/api/object/sync/config")
                .content(Mapper.INSTANCE.get().writeValueAsString(request))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$.objectSyncConfig").isNotEmpty)
            .andExpect(jsonPath("$.objectSyncConfig.id").isNotEmpty)
            .andExpect(jsonPath("$.objectSyncConfig.connectorInstanceId").value(request.connectorInstanceId.toString()))
            .andExpect(jsonPath("$.objectSyncConfig.enabled").value(request.enabled))
            .andExpect(jsonPath("$.objectSyncConfig.objectTypeId").value(request.objectTypeId.toString()))
            .andExpect(jsonPath("$.errors").isEmpty)
    }
}