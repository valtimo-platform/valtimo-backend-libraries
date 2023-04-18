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
import com.ritense.processlink.domain.CustomProcessLinkMapper
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.service.ProcessLinkActivityService
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets
import java.util.UUID

internal class ProcessLinkTaskResourceTest {

    lateinit var mockMvc: MockMvc
    lateinit var processLinkActivityService: ProcessLinkActivityService
    lateinit var processLinkMappers: List<ProcessLinkMapper>
    lateinit var processLinkTaskResource: ProcessLinkTaskResource
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun init() {
        objectMapper = jacksonObjectMapper()
        processLinkActivityService = mock()
        processLinkMappers = listOf(CustomProcessLinkMapper(objectMapper))
        processLinkTaskResource = ProcessLinkTaskResource(processLinkActivityService)

        val mappingJackson2HttpMessageConverter = MappingJackson2HttpMessageConverter()
        mappingJackson2HttpMessageConverter.objectMapper = objectMapper

        mockMvc = MockMvcBuilders
            .standaloneSetup(processLinkTaskResource)
            .setMessageConverters(mappingJackson2HttpMessageConverter)
            .build()
    }


    @Test
    fun `should list process links`() {
        val taskId = UUID.randomUUID()


        val processLinkActivityResult = ProcessLinkActivityResult("test", mapOf("x" to "y"))
        whenever(processLinkActivityService.openTask(taskId)).thenReturn(processLinkActivityResult)

        mockMvc.perform(
            get("/api/v2/process-link/task/$taskId")
                .characterEncoding(StandardCharsets.UTF_8.name())
                .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.type").value("test"))
            .andExpect(jsonPath("$.properties.x").value("y"))

        verify(processLinkActivityService).openTask(taskId)
    }
}
