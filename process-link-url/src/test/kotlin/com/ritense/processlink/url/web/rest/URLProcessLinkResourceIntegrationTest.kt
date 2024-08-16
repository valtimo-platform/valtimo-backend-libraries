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

package com.ritense.processlink.url.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.processlink.autodeployment.ProcessLinkDeploymentApplicationReadyEventListener
import com.ritense.processlink.domain.ActivityTypeWithEventName.SERVICE_TASK_START
import com.ritense.processlink.repository.ProcessLinkRepository
import com.ritense.processlink.url.BaseIntegrationTest
import com.ritense.processlink.url.domain.URLProcessLink
import com.ritense.processlink.url.web.rest.dto.URLProcessLinkCreateRequestDto
import com.ritense.processlink.url.web.rest.dto.URLProcessLinkUpdateRequestDto
import java.nio.charset.StandardCharsets
import java.util.UUID
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


@Transactional
internal class URLProcessLinkResourceIntegrationTest @Autowired constructor(
    private val webApplicationContext: WebApplicationContext,
    private val processLinkRepository: ProcessLinkRepository,
    private val listener: ProcessLinkDeploymentApplicationReadyEventListener
) : BaseIntegrationTest() {

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should create a process-link`() {
        val createDto = URLProcessLinkCreateRequestDto(
            processDefinitionId = PROCESS_DEF_ID,
            activityId = ACTIVITY_ID,
            activityType = SERVICE_TASK_START,
            url = "https://www.ritense.nl"
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
    fun `should create a process-link without processLinkType`() {
        val createDto = URLProcessLinkCreateRequestDto(
            processDefinitionId = PROCESS_DEF_ID,
            activityId = ACTIVITY_ID,
            activityType = SERVICE_TASK_START,
            url = "https://www.ritense.nl"
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
    fun `should update a process-link`() {
        val processLinkId = createProcessLink()

        val updateDto = URLProcessLinkUpdateRequestDto(
            id = processLinkId,
            url = "https://www.ritense.nl"
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

    @Test
    fun `should export process-links`() {
        listener.deployProcessLinks()

        mockMvc.perform(
            get("/api/v1/process-link/export")
                .param("processDefinitionKey", "url-one-task-process")
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].activityId").value( "start-event"))
            .andExpect(jsonPath("$[0].activityType").value( "bpmn:StartEvent:start"))
            .andExpect(jsonPath("$[0].processLinkType").value( "url"))
            .andExpect(jsonPath("$[0].url").value( "https://www.ritense.nl"))
            .andExpect(jsonPath("$[1].activityId").value( "do-something"))
            .andExpect(jsonPath("$[1].activityType").value( "bpmn:UserTask:start"))
            .andExpect(jsonPath("$[1].processLinkType").value( "url"))
            .andExpect(jsonPath("$[1].url").value( "https://www.ritense.nl"))
    }

    private fun createProcessLink(): UUID {
        return processLinkRepository.save(
            URLProcessLink(
                UUID.randomUUID(),
                processDefinitionId = PROCESS_DEF_ID,
                activityId = ACTIVITY_ID,
                activityType = SERVICE_TASK_START,
                url = "https://www.ritense.nl"
            )
        ).id
    }

    companion object {
        const val PROCESS_DEF_ID = "test-process"
        const val ACTIVITY_ID = "test-activity"
    }
}
