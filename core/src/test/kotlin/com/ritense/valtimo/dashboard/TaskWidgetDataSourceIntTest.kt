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

package com.ritense.document.dashboard

import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.authorization.UserManagementServiceHolder
import com.ritense.valtimo.contract.dashboard.QueryCondition
import com.ritense.valtimo.contract.repository.ExpressionOperator
import com.ritense.valtimo.dashboard.TaskCountDataSourceProperties
import com.ritense.valtimo.dashboard.TaskWidgetDataSource
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.task.Task
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class TaskWidgetDataSourceIntTest @Autowired constructor(
    private val taskWidgetDataSource: TaskWidgetDataSource
) : BaseIntegrationTest() {

    @BeforeEach
    fun setup() {
        UserManagementServiceHolder(userManagementService)

        whenever(userManagementService.currentUser.id)
            .thenReturn(mockedUserId)

        whenever(userManagementService.currentUser.email)
            .thenReturn(mockedUserEmail)

        whenever(userManagementService.currentUser.userIdentifier)
            .thenReturn(mockedUserIdentifier)
    }

    @Test
    fun `should count amount of tasks`() {
        val expectedCount = 3

        repeat(expectedCount) {
            createTask()
        }

        val dataSourceProperties: TaskCountDataSourceProperties = mock()

        val result = taskWidgetDataSource.getTaskCount(dataSourceProperties)

        assertThat(result.value).isEqualTo(expectedCount.toLong())
        assertThat(result.total).isEqualTo(expectedCount.toLong())
    }

    @Test
    fun `should count amount of tasks with criteria`() {
        val defaultTasks = 2

        repeat(defaultTasks) {
            createTask()
        }

        createTask("Test2", "test2@test2.com")

        val dataSourceProperties = TaskCountDataSourceProperties(
            queryConditions = listOf(
                QueryCondition("task:assignee", ExpressionOperator.EQUAL_TO, defaultAssignee)
            )
        )

        val result = taskWidgetDataSource.getTaskCount(dataSourceProperties)

        assertThat(result.value).isEqualTo(defaultTasks.toLong())
        assertThat(result.total).isEqualTo(3.toLong())
    }


    @Test
    fun `should filter out null values`() {
        val defaultTasks = 2

        repeat(defaultTasks) {
            createTask()
        }

        createTask(null)

        val dataSourceProperties = TaskCountDataSourceProperties(
            queryConditions = listOf(
                QueryCondition("task:name", ExpressionOperator.NOT_EQUAL_TO, "\${null}")
            )
        )

        val result = taskWidgetDataSource.getTaskCount(dataSourceProperties)

        assertThat(result.value).isEqualTo(defaultTasks.toLong())
        assertThat(result.total).isEqualTo(3.toLong())
    }

    @Test
    fun `should handle local date time now filter`() {
        createTask()

        Thread.sleep(2000)

        createTask()

        val queryConditions = listOf(
            QueryCondition(
                "task:createTime",
                ExpressionOperator.GREATER_THAN,
                "\${localDateTimeNow.minusSeconds(1)}"
            )
        )

        val properties = TaskCountDataSourceProperties(queryConditions = queryConditions)

        val result = taskWidgetDataSource.getTaskCount(properties)

        assertThat(result.value).isEqualTo(1)
        assertThat(result.total).isEqualTo(2)
    }

    @Test
    fun `should support current user criteria`() {
        createTask(mockedUserId)
        createTask(mockedUserEmail)
        createTask(mockedUserIdentifier)

        val properties1 = TaskCountDataSourceProperties(
            queryConditions = listOf(
                QueryCondition(
                    "task:name",
                    ExpressionOperator.EQUAL_TO,
                    "\${currentUserId}"
                )
            )
        )
        val result1 = taskWidgetDataSource.getTaskCount(properties1)

        val properties2 = TaskCountDataSourceProperties(
            queryConditions = listOf(
                QueryCondition(
                    "task:name",
                    ExpressionOperator.EQUAL_TO,
                    "\${currentUserIdentifier}"
                )
            )
        )
        val result2 = taskWidgetDataSource.getTaskCount(properties2)

        val properties3 = TaskCountDataSourceProperties(
            queryConditions = listOf(
                QueryCondition(
                    "task:name",
                    ExpressionOperator.EQUAL_TO,
                    "\${currentUserEmail}"
                )
            )
        )
        val result3 = taskWidgetDataSource.getTaskCount(properties3)

        assertThat(result1.value).isEqualTo(1)
        assertThat(result2.value).isEqualTo(1)
        assertThat(result3.value).isEqualTo(1)
        assertThat(result1.total).isEqualTo(3)
        assertThat(result2.total).isEqualTo(3)
        assertThat(result3.total).isEqualTo(3)
    }


    private fun createTask(
        name: String? = "Test",
        assignee: String = defaultAssignee,
    ) {
        val task: Task = camundaTaskService.newTask()
        task.setName(name)
        task.setAssignee(assignee)

        camundaTaskService.saveTask(task)
    }

    companion object {
        private val mockedUserId = "mockUserId"
        private val mockedUserEmail = "mockUserEmail"
        private val mockedUserIdentifier = "mockUserIdentifier"
        private val defaultAssignee = "test@test.com"
    }
}