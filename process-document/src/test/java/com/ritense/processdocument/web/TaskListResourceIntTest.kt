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

package com.ritense.processdocument.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.domain.DisplayType
import com.ritense.case.domain.EmptyDisplayTypeParameter
import com.ritense.case.domain.TaskListColumn
import com.ritense.case.domain.TaskListColumnId
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.web.request.TaskListSearchDto
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.service.CamundaTaskService
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.hasKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

class TaskListResourceIntTest : BaseIntegrationTest() {

    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    lateinit var taskListColumnRepository: TaskListColumnRepository

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Autowired
    lateinit var taskService: TaskService

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        val allProcessIds = runtimeService.createProcessInstanceQuery().list().map { it.id }
        runtimeService.deleteProcessInstances(allProcessIds, "Cleaning for tests", true, false)
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [AuthoritiesConstants.USER])
    @Transactional
    fun `should get task list for caseDefinitionName`() {
        val taskListColumns = listOf(
            TaskListColumn(
                TaskListColumnId("house", "col1"),
                "First name",
                "doc:first-name",
                DisplayType("string", EmptyDisplayTypeParameter()),
                false,
                null,
                0
            ),
            TaskListColumn(
                TaskListColumnId("house", "col2"),
                "Last name",
                "doc:last-name",
                DisplayType("string", EmptyDisplayTypeParameter()),
                true,
                ColumnDefaultSort.ASC,
                1
            )
        )

        taskListColumnRepository.saveAllAndFlush(taskListColumns)

        val testUserData = startNewProcessesWithTestData()

        val taskList = taskService.createTaskQuery().active().list()

        assertThat(taskList).hasSize(testUserData.size)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v3/task")
                .param("filter", CamundaTaskService.TaskFilter.ALL.toString())
                .content(objectMapper.writeValueAsString(TaskListSearchDto("house")))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[*].id", hasItems(*taskList.map { it.id }.toTypedArray())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].items").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].items[0].key").value("col1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].items[0].value").value(testUserData[0]["firstName"]))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].items[1].key").value("col2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].items[1].value").value(testUserData[0]["lastName"]))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].items[0].key").value("col1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].items[0].value").value(testUserData[1]["firstName"]))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].items[1].key").value("col2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].items[1].value").value(testUserData[1]["lastName"]))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].items[0].key").value("col1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].items[0].value").value(testUserData[2]["firstName"]))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].items[1].key").value("col2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].items[1].value").value(testUserData[2]["lastName"]))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(3))
    }



    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [AuthoritiesConstants.USER])
    @Transactional
    fun `should get task list without caseDefinitionName`() {
        val testUserData = startNewProcessesWithTestData()

        val taskList = taskService.createTaskQuery().active().list()

        assertThat(taskList).hasSize(testUserData.size)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v3/task")
                .param("filter", CamundaTaskService.TaskFilter.ALL.toString())
                .content(objectMapper.writeValueAsString(TaskListSearchDto(null)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[*].id", hasItems(*taskList.map { it.id }.toTypedArray())))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0]", hasKey("name")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0]", hasKey("assignee")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0]", hasKey("created")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0]", hasKey("due")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0]", hasKey("owner")))
    }

    private fun startNewProcessesWithTestData(): List<Map<String, String>> {
        val testUserData = listOf(
            mapOf("lastName" to "Ever", "firstName" to "Greatest"),
            mapOf("lastName" to "A.", "firstName" to "Henk"),
            mapOf("lastName" to "R.", "firstName" to "Karel")
        )

        testUserData.forEach {
            val startRequest = NewDocumentAndStartProcessRequest(
                "single-user-task-process",
                NewDocumentRequest(
                    "house",
                    objectMapper.readTree("""{ "first-name": "${it["firstName"]}", "last-name": "${it["lastName"]}"}""")
                )
            )
            AuthorizationContext.runWithoutAuthorization {
                camundaProcessJsonSchemaDocumentService.newDocumentAndStartProcess(
                    startRequest
                )
            }
        }
        return testUserData
    }
}