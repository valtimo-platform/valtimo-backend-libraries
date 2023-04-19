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

package com.ritense.processlink.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.CustomProcessLink
import com.ritense.processlink.domain.CustomProcessLinkCreateRequestDto
import com.ritense.processlink.domain.CustomProcessLinkMapper
import com.ritense.processlink.domain.CustomProcessLinkUpdateRequestDto
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.contract.json.Mapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets
import java.util.UUID

internal class ProcessLinkResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var processLinkService: ProcessLinkService
    lateinit var processLinkMappers: List<ProcessLinkMapper>
    lateinit var processLinkResource: ProcessLinkResource
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun init() {
        objectMapper = jacksonObjectMapper()
        processLinkService = mock()
        processLinkMappers = listOf(CustomProcessLinkMapper(objectMapper))
        processLinkResource = ProcessLinkResource(processLinkService, processLinkMappers)

        val mappingJackson2HttpMessageConverter = MappingJackson2HttpMessageConverter()
        mappingJackson2HttpMessageConverter.objectMapper = objectMapper

        mockMvc = MockMvcBuilders
            .standaloneSetup(processLinkResource)
            .setMessageConverters(mappingJackson2HttpMessageConverter)
            .build()
    }


    @Test
    fun `should list process links`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val processDefinitionId = "pid"
        val activityId = "aid"
        val activityType = ActivityTypeWithEventName.SERVICE_TASK_START

        val processLinks = listOf(
            CustomProcessLink(
                id = id1,
                processDefinitionId = processDefinitionId,
                activityId = activityId,
                activityType = activityType
            ),
            CustomProcessLink(
                id = id2,
                processDefinitionId = processDefinitionId,
                activityId = activityId,
                activityType = activityType
            )
        )

        whenever(processLinkService.getProcessLinks(any(), any())).thenReturn(processLinks)

        mockMvc.perform(
            get("/api/v1/process-link?processDefinitionId=$processDefinitionId&activityId=$activityId")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.[0].id").value(id1.toString()))
            .andExpect(jsonPath("$.[0].processDefinitionId").value(processDefinitionId))
            .andExpect(jsonPath("$.[0].activityId").value(activityId))
            .andExpect(jsonPath("$.[0].activityType").value(activityType.value))
            .andExpect(jsonPath("$.[1].id").value(id2.toString()))
            .andExpect(jsonPath("$.[1].processDefinitionId").value(processDefinitionId))
            .andExpect(jsonPath("$.[1].activityId").value(activityId))
            .andExpect(jsonPath("$.[1].activityType").value(activityType.value))


        verify(processLinkService).getProcessLinks(processDefinitionId, activityId)
    }

    @Test
    fun `should add process link`() {
        val processLinkDto = CustomProcessLinkCreateRequestDto(
            processDefinitionId = UUID.randomUUID().toString(),
            activityId = "someActivity",
            activityType = ActivityTypeWithEventName.SERVICE_TASK_START
        )

        mockMvc.perform(
            post("/api/v1/process-link")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Mapper.INSTANCE.get().writeValueAsString(processLinkDto))
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        verify(processLinkService).createProcessLink(processLinkDto)
    }

    @Test
    fun `should update process link`() {
        val processLinkDto = CustomProcessLinkUpdateRequestDto(
            id = UUID.randomUUID(),
        )

        mockMvc.perform(
            put("/api/v1/process-link")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(Mapper.INSTANCE.get().writeValueAsString(processLinkDto))
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)

        verify(processLinkService).updateProcessLink(processLinkDto)
    }

    @Test
    fun `should delete process link`() {
        val processLinkId = UUID.randomUUID()

        mockMvc.perform(delete("/api/v1/process-link/{processLinkId}", processLinkId))
            .andDo(print())
            .andExpect(status().isNoContent)

        verify(processLinkService).deleteProcessLink(processLinkId)
    }
}
