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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.processlink.BaseIntegrationTest
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import com.ritense.processlink.domain.CustomProcessLink
import com.ritense.processlink.domain.CustomProcessLinkCreateRequestDto
import com.ritense.processlink.domain.CustomProcessLinkUpdateRequestDto
import com.ritense.processlink.repository.ProcessLinkRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.StandardCharsets
import java.util.UUID


internal class ProcessLinkResourceIT : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var processLinkRepository: ProcessLinkRepository


    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    @Transactional
    fun `should create a process-link`() {
        val createDto = CustomProcessLinkCreateRequestDto(
            processDefinitionId = PROCESS_DEF_ID,
            activityId = ACTIVITY_ID,
            activityType = SERVICE_TASK_START
        )

        mockMvc.perform(
            post("/api/v1/process-link")
                .content(ObjectMapper().writeValueAsString(createDto))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)
    }

    @Test
    @Transactional
    fun `should create a process-link without processLinkType`() {
        val createDto = CustomProcessLinkCreateRequestDto(
            processDefinitionId = PROCESS_DEF_ID,
            activityId = ACTIVITY_ID,
            activityType = SERVICE_TASK_START
        )
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.valueToTree<ObjectNode>(createDto)
        jsonNode.remove("processLinkType")
        val body = objectMapper.writeValueAsString(jsonNode)

        mockMvc.perform(
            post("/api/v1/process-link")
                .content(body)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)
    }

    @Test
    @Transactional
    fun `should list process-links`() {
        createProcessLink()

        mockMvc.perform(
            get("/api/v1/process-link")
                .param("processDefinitionId", PROCESS_DEF_ID)
                .param("activityId", ACTIVITY_ID)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.*", hasSize<Int>(1)))
    }

    @Test
    @Transactional
    fun `should update a process-link`() {
        val processLinkId = createProcessLink()

        val updateDto = CustomProcessLinkUpdateRequestDto(
            id = processLinkId
        )

        mockMvc.perform(
            put("/api/v1/process-link")
                .content(ObjectMapper().writeValueAsString(updateDto))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isNoContent)
    }

    private fun createProcessLink(): UUID {
        return processLinkRepository.save(
            CustomProcessLink(
                UUID.randomUUID(),
                processDefinitionId = PROCESS_DEF_ID,
                activityId = ACTIVITY_ID,
                activityType = SERVICE_TASK_START
            )
        ).id
    }

    companion object {
        const val PROCESS_DEF_ID = "test-process"
        const val ACTIVITY_ID = "test-activity"
    }
}
