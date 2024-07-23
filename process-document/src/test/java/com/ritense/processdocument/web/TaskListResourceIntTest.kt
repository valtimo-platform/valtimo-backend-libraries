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
import com.ritense.case.domain.EnumDisplayTypeParameter
import com.ritense.case.domain.TaskListColumn
import com.ritense.case.domain.TaskListColumnId
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.tasksearch.SearchWithConfigRequest
import com.ritense.processdocument.tasksearch.TaskListSearchFieldV2Dto
import com.ritense.processdocument.web.request.TaskListSearchDto
import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.service.CamundaTaskService
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

private const val DOCUMENT_DEFINITION_NAME = "house"

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

    @Autowired
    lateinit var searchFieldV2Service: SearchFieldV2Service

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
                TaskListColumnId(DOCUMENT_DEFINITION_NAME, "col1"),
                "First name",
                "doc:first-name",
                DisplayType("string", EmptyDisplayTypeParameter()),
                false,
                null,
                0
            ),
            TaskListColumn(
                TaskListColumnId(DOCUMENT_DEFINITION_NAME, "col2"),
                "Last name",
                "doc:last-name",
                DisplayType("string", EmptyDisplayTypeParameter()),
                true,
                ColumnDefaultSort.ASC,
                1
            ),
            TaskListColumn(
                TaskListColumnId(DOCUMENT_DEFINITION_NAME, "col3"),
                "Context",
                "pv:context",
                DisplayType("string", EmptyDisplayTypeParameter()),
                true,
                ColumnDefaultSort.ASC,
                2
            ),
            TaskListColumn(
                TaskListColumnId(DOCUMENT_DEFINITION_NAME, "col4"),
                "Approved",
                "pv:approved",
                DisplayType("boolean", EnumDisplayTypeParameter(mapOf("Yes" to "No"))),
                true,
                ColumnDefaultSort.ASC,
                3
            )
        )

        taskListColumnRepository.saveAllAndFlush(taskListColumns)

        val testUserData = startNewProcessesWithTestData()

        val taskList = taskService.createTaskQuery().active().list()

        assertThat(taskList).hasSize(testUserData.size)

        val sortedTestUserData = testUserData.sortedBy { it["lastName"] as String? }

        mockMvc.perform(
            post("/api/v3/task")
                .param("filter", CamundaTaskService.TaskFilter.ALL.toString())
                .content(objectMapper.writeValueAsString(TaskListSearchDto(DOCUMENT_DEFINITION_NAME)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[*].id", hasItems(*taskList.map { it.id }.toTypedArray())))
            .andExpect(jsonPath("$.content[0].items").isArray)
            .andExpect(jsonPath("$.content[0].items[0].key").value("col1"))
            .andExpect(jsonPath("$.content[0].items[0].value").value(sortedTestUserData[0]["firstName"]))
            .andExpect(jsonPath("$.content[0].items[1].key").value("col2"))
            .andExpect(jsonPath("$.content[0].items[1].value").value(sortedTestUserData[0]["lastName"]))
            .andExpect(jsonPath("$.content[0].items[2].key").value("col3"))
            .andExpect(jsonPath("$.content[0].items[2].value").value(sortedTestUserData[0]["context"]))
            .andExpect(jsonPath("$.content[0].items[3].key").value("col4"))
            .andExpect(jsonPath("$.content[0].items[3].value").value(sortedTestUserData[0]["approved"]))
            .andExpect(jsonPath("$.content[1].items[0].key").value("col1"))
            .andExpect(jsonPath("$.content[1].items[0].value").value(sortedTestUserData[1]["firstName"]))
            .andExpect(jsonPath("$.content[1].items[1].key").value("col2"))
            .andExpect(jsonPath("$.content[1].items[1].value").value(sortedTestUserData[1]["lastName"]))
            .andExpect(jsonPath("$.content[1].items[2].key").value("col3"))
            .andExpect(jsonPath("$.content[1].items[2].value").value(sortedTestUserData[1]["context"]))
            .andExpect(jsonPath("$.content[1].items[3].key").value("col4"))
            .andExpect(jsonPath("$.content[1].items[3].value").value(sortedTestUserData[1]["approved"]))
            .andExpect(jsonPath("$.content[2].items[0].key").value("col1"))
            .andExpect(jsonPath("$.content[2].items[0].value").value(sortedTestUserData[2]["firstName"]))
            .andExpect(jsonPath("$.content[2].items[1].key").value("col2"))
            .andExpect(jsonPath("$.content[2].items[1].value").value(sortedTestUserData[2]["lastName"]))
            .andExpect(jsonPath("$.content[2].items[2].key").value("col3"))
            .andExpect(jsonPath("$.content[2].items[2].value").value(sortedTestUserData[2]["context"]))
            .andExpect(jsonPath("$.content[2].items[3].key").value("col4"))
            .andExpect(jsonPath("$.content[2].items[3].value").value(sortedTestUserData[2]["approved"]))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.sort[0].property").value("doc:last-name"))
            .andExpect(jsonPath("$.sort[0].direction").value("ASC"))
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [AuthoritiesConstants.USER])
    @Transactional
    fun `should get task list without caseDefinitionName`() {
        val testUserData = startNewProcessesWithTestData()

        val taskList = taskService.createTaskQuery().active().list()

        assertThat(taskList).hasSize(testUserData.size)

        mockMvc.perform(
            post("/api/v3/task")
                .param("filter", CamundaTaskService.TaskFilter.ALL.toString())
                .content(objectMapper.writeValueAsString(TaskListSearchDto(null)))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[*].id", hasItems(*taskList.map { it.id }.toTypedArray())))
            .andExpect(jsonPath("$.content[0]", hasKey("name")))
            .andExpect(jsonPath("$.content[0]", hasKey("assignee")))
            .andExpect(jsonPath("$.content[0]", hasKey("created")))
            .andExpect(jsonPath("$.content[0]", hasKey("due")))
            .andExpect(jsonPath("$.content[0]", hasKey("owner")))
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [AuthoritiesConstants.USER])
    @Transactional
    fun `should filter on something`() {
        val firstNameToSearch = "Henk"

        val taskListColumns = listOf(
            TaskListColumn(
                TaskListColumnId(DOCUMENT_DEFINITION_NAME, "col1"),
                "First name",
                "doc:first-name",
                DisplayType("string", EmptyDisplayTypeParameter()),
                false,
                null,
                0
            ),
            TaskListColumn(
                TaskListColumnId(DOCUMENT_DEFINITION_NAME, "col2"),
                "Last name",
                "doc:last-name",
                DisplayType("string", EmptyDisplayTypeParameter()),
                true,
                ColumnDefaultSort.ASC,
                1
            ),
            TaskListColumn(
                TaskListColumnId(DOCUMENT_DEFINITION_NAME, "col3"),
                "Context",
                "pv:context",
                DisplayType("string", EmptyDisplayTypeParameter()),
                true,
                ColumnDefaultSort.ASC,
                2
            ),
            TaskListColumn(
                TaskListColumnId(DOCUMENT_DEFINITION_NAME, "col4"),
                "Approved",
                "pv:approved",
                DisplayType("boolean", EnumDisplayTypeParameter(mapOf("Yes" to "No"))),
                true,
                ColumnDefaultSort.ASC,
                3
            )
        )

        taskListColumnRepository.saveAllAndFlush(taskListColumns)

        searchFieldV2Service.create(TaskListSearchFieldV2Dto(
            ownerId = DOCUMENT_DEFINITION_NAME,
            key = "firstName",
            title = "First name",
            path = "doc:first-name",
            order = 0,
            dataType = DataType.TEXT,
            matchType = SearchFieldMatchType.LIKE,
            fieldType = FieldType.SINGLE
        ))

        val testUserData = startNewProcessesWithTestData()

        val taskList = taskService.createTaskQuery().active().list()

        assertThat(taskList).hasSize(testUserData.size)

        val expectedUserData = testUserData.find { it["firstName"] == firstNameToSearch }!!

        val filter = SearchWithConfigRequest.SearchWithConfigFilter()
        filter.key = "firstName"
        filter.setValues(listOf(firstNameToSearch))

        val searchWithConfigRequest = SearchWithConfigRequest()
        searchWithConfigRequest.otherFilters = listOf(filter)

        mockMvc.perform(
            post("/api/v1/document-definition/{caseDefinitionName}/task/search", DOCUMENT_DEFINITION_NAME)
                .content(objectMapper.writeValueAsString(searchWithConfigRequest))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content", hasSize<Any>(1)))
            .andExpect(jsonPath("$.content[0].items").isArray)
            .andExpect(jsonPath("$.content[0].items[0].key").value("col1"))
            .andExpect(jsonPath("$.content[0].items[0].value").value(expectedUserData["firstName"]))
            .andExpect(jsonPath("$.content[0].items[1].key").value("col2"))
            .andExpect(jsonPath("$.content[0].items[1].value").value(expectedUserData["lastName"]))
            .andExpect(jsonPath("$.content[0].items[2].key").value("col3"))
            .andExpect(jsonPath("$.content[0].items[2].value").value(expectedUserData["context"]))
            .andExpect(jsonPath("$.content[0].items[3].key").value("col4"))
            .andExpect(jsonPath("$.content[0].items[3].value").value(expectedUserData["approved"]))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.sort[0].property").value("doc:last-name"))
            .andExpect(jsonPath("$.sort[0].direction").value("ASC"))
    }

    private fun startNewProcessesWithTestData(): List<Map<String, Any?>> {
        val testUserData = listOf(
            mapOf("lastName" to "Ever", "firstName" to "Greatest", "context" to "Amsterdam", "approved" to true),
            mapOf("lastName" to "A.", "firstName" to "Henk", "context" to "Rotterdam", "approved" to false),
            mapOf("lastName" to "R.", "firstName" to null, "context" to null, "approved" to null)
        )

        testUserData.forEach { testUserDataRow ->
            val firstName = testUserDataRow["firstName"]?.let { "\"$it\"" }
            val lastName = testUserDataRow["lastName"]?.let { "\"$it\"" }
            val context = testUserDataRow["context"]
            val approved = testUserDataRow["approved"]
            val startRequest = NewDocumentAndStartProcessRequest(
                "single-user-task-process",
                NewDocumentRequest(
                    DOCUMENT_DEFINITION_NAME,
                    objectMapper.readTree("""{ "first-name": $firstName, "last-name": $lastName}""")
                )
            ).withProcessVars(mapOf("context" to context, "approved" to approved))
            AuthorizationContext.runWithoutAuthorization {
                camundaProcessJsonSchemaDocumentService.newDocumentAndStartProcess(
                    startRequest
                )
            }
        }
        return testUserData
    }
}