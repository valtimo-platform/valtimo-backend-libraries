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

package com.ritense.processlink.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.processlink.BaseIntegrationTest
import com.ritense.processlink.service.ProcessLinkActivityService
import com.ritense.valtimo.service.CamundaProcessService
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
@Import(ProcessLinkActivityService::class)
internal class ProcessLinkTaskResourceIT @Autowired constructor(
    private val webApplicationContext: WebApplicationContext,
    private val camundaProcessService: CamundaProcessService,
) : BaseIntegrationTest() {

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webApplicationContext)
            .build()
    }

    @Test
    fun `should list tasks with process links`() {
        val processInstanceWithDefinition =
            runWithoutAuthorization { camundaProcessService.startProcess(PROCESS_DEF_ID, "", emptyMap()) }

        runWithoutAuthorization {
            mockMvc.perform(
                get(
                    "/api/v1/process/{processInstanceId}/tasks/process-link",
                    processInstanceWithDefinition.processInstanceDto.id
                )
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isNotEmpty)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.*", hasSize<Int>(1)))
        }
    }


    companion object {
        const val PROCESS_DEF_ID = "test-process"
    }
}