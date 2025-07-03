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

package com.ritense.valtimo.operaton.repository

import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.service.OperatonTaskService
import org.assertj.core.api.Assertions
import org.operaton.bpm.engine.IdentityService
import org.operaton.bpm.engine.runtime.ProcessInstance
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

class OperatonHistoricProcessInstanceSpecificationHelperIntTest @Autowired constructor(
    private val operatonHistoricProcessInstanceRepository: OperatonHistoricProcessInstanceRepository,
    private val taskService: OperatonTaskService,
    private val identityService: IdentityService
) :
    BaseIntegrationTest() {

    private var originalUserId: String? = null
    private lateinit var processInstancesMap: Map<String, List<ProcessInstance>>
    private lateinit var createDate: LocalDateTime

    @BeforeEach
    fun prepare() {
        createDate = LocalDateTime.now()
        originalUserId = identityService.currentAuthentication?.userId

        processInstancesMap = mapOf(
            TEST_PROCESS to (1..3)
                .map {
                    runtimeService.startProcessInstanceByKey(
                        TEST_PROCESS,
                        UUID.randomUUID().toString(),
                        mapOf("myVariable" to true)
                    )
                }.toList(),
            USER_TASK_PROCESS to run {
                identityService.setAuthenticatedUserId("johndoe")
                listOf(
                    runtimeService.startProcessInstanceByKey(
                        USER_TASK_PROCESS,
                        UUID.randomUUID().toString(),
                        mapOf("myUserVariable" to true)
                    )
                )
            }
        )
    }

    @AfterEach
    fun finish() {
        identityService.setAuthenticatedUserId(originalUserId)
    }

    @Test
    @Transactional
    fun query() {
        val result = operatonHistoricProcessInstanceRepository.findAll(OperatonHistoricProcessInstanceSpecificationHelper.query())
            .map { it.id }

        Assertions.assertThat(result).isNotEmpty
        Assertions.assertThat(result).containsAll(operatonHistoricProcessInstanceRepository.findAll().map { it.id })
    }

    @Test
    @Transactional
    fun byId() {
        val id = getRandomProcessInstance().id

        val instance = operatonHistoricProcessInstanceRepository.findOne(
            OperatonHistoricProcessInstanceSpecificationHelper.byId(id)
        ).get()

        Assertions.assertThat(instance.id).isEqualTo(id)
    }

    @Test
    @Transactional
    fun byProcessInstanceId() {
        val processInstanceId = getRandomProcessInstance().processInstanceId

        val instance = operatonHistoricProcessInstanceRepository.findOne(
            OperatonHistoricProcessInstanceSpecificationHelper.byProcessInstanceId(processInstanceId)
        ).get()

        Assertions.assertThat(instance.processInstanceId).isEqualTo(processInstanceId)
    }

    @Test
    @Transactional
    fun byProcessDefinitionKey() {
        val instanceIds = operatonHistoricProcessInstanceRepository.findAll(
            OperatonHistoricProcessInstanceSpecificationHelper.byProcessDefinitionKey(TEST_PROCESS)
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    fun byUnfinished() {
        val instanceIds = operatonHistoricProcessInstanceRepository.findAll(
            OperatonHistoricProcessInstanceSpecificationHelper.byUnfinished()
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    fun byFinished() {
        val instanceIds = operatonHistoricProcessInstanceRepository.findAll(
            OperatonHistoricProcessInstanceSpecificationHelper.byFinished()
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    @WithMockUser(username = TEST_USER, authorities = [ADMIN])
    fun byEndTimeAfter() {
        Thread.sleep(1000)
        val now = LocalDateTime.now()
        val processInstanceId = getRandomProcessInstanceByKey(USER_TASK_PROCESS).processInstanceId
        val task = taskService.findTask(OperatonTaskSpecificationHelper.byProcessInstanceId(processInstanceId))
        Thread.sleep(1000)
        taskService.complete(task.id)

        val instanceIds = operatonHistoricProcessInstanceRepository.findAll(
            OperatonHistoricProcessInstanceSpecificationHelper.byEndTimeAfter(now)
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    @WithMockUser(username = TEST_USER, authorities = [ADMIN])
    fun byEndTimeBefore() {
        Thread.sleep(1000)
        val now = LocalDateTime.now()
        val processInstanceId = getRandomProcessInstanceByKey(USER_TASK_PROCESS).processInstanceId
        val task = taskService.findTask(OperatonTaskSpecificationHelper.byProcessInstanceId(processInstanceId))
        Thread.sleep(1000)
        taskService.complete(task.id)

        val instanceIds = operatonHistoricProcessInstanceRepository.findAll(
            OperatonHistoricProcessInstanceSpecificationHelper.byEndTimeBefore(now)
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    fun byStartUserId() {
        val processInstance = getRandomProcessInstanceByKey(USER_TASK_PROCESS)

        val instanceIds = operatonHistoricProcessInstanceRepository.findAll(
            OperatonHistoricProcessInstanceSpecificationHelper.byStartUserId("johndoe")
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsExactly(processInstance.id)
    }

    private fun getRandomProcessInstance() = processInstancesMap.values.flatten().random()
    private fun getRandomProcessInstanceByKey(key: String) = processInstancesMap[key]!!.random()

    private fun getProcessInstancesByKey(key: String) = processInstancesMap[key]!!

    companion object {
        const val TEST_PROCESS = "test-process"
        const val USER_TASK_PROCESS = "user-task-process"
        private const val TEST_USER = "user@valtimo.nl"
    }
}