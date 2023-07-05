package com.ritense.valtimo.camunda.repository

import com.ritense.valtimo.BaseIntegrationTest
import java.time.LocalDateTime
import java.util.UUID
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class CamundaHistoricProcessInstanceSpecificationHelperIntTest @Autowired constructor(
    private val camundaHistoricProcessInstanceRepository: CamundaHistoricProcessInstanceRepository,
    private val taskService: TaskService,
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
        val result = camundaHistoricProcessInstanceRepository.findAll(CamundaHistoricProcessInstanceSpecificationHelper.query())
            .map { it.id }

        Assertions.assertThat(result).isNotEmpty
        Assertions.assertThat(result).containsAll(camundaHistoricProcessInstanceRepository.findAll().map { it.id })
    }

    @Test
    @Transactional
    fun byId() {
        val id = getRandomProcessInstance().id

        val instance = camundaHistoricProcessInstanceRepository.findOne(
            CamundaHistoricProcessInstanceSpecificationHelper.byId(id)
        ).get()

        Assertions.assertThat(instance.id).isEqualTo(id)
    }

    @Test
    @Transactional
    fun byProcessInstanceId() {
        val processInstanceId = getRandomProcessInstance().processInstanceId

        val instance = camundaHistoricProcessInstanceRepository.findOne(
            CamundaHistoricProcessInstanceSpecificationHelper.byProcessInstanceId(processInstanceId)
        ).get()

        Assertions.assertThat(instance.processInstanceId).isEqualTo(processInstanceId)
    }

    @Test
    @Transactional
    fun byProcessDefinitionKey() {
        val instanceIds = camundaHistoricProcessInstanceRepository.findAll(
            CamundaHistoricProcessInstanceSpecificationHelper.byProcessDefinitionKey(TEST_PROCESS)
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    fun byUnfinished() {
        val instanceIds = camundaHistoricProcessInstanceRepository.findAll(
            CamundaHistoricProcessInstanceSpecificationHelper.byUnfinished()
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    fun byFinished() {
        val instanceIds = camundaHistoricProcessInstanceRepository.findAll(
            CamundaHistoricProcessInstanceSpecificationHelper.byFinished()
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    fun byEndTimeAfter() {
        val now = LocalDateTime.now()
        val processInstanceId = getRandomProcessInstanceByKey(USER_TASK_PROCESS).processInstanceId
        val task = taskService.createTaskQuery()
            .processInstanceId(processInstanceId)
            .singleResult()
        taskService.complete(task.id)

        val instanceIds = camundaHistoricProcessInstanceRepository.findAll(
            CamundaHistoricProcessInstanceSpecificationHelper.byEndTimeAfter(now)
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    fun byEndTimeBefore() {
        val now = LocalDateTime.now()
        val processInstanceId = getRandomProcessInstanceByKey(USER_TASK_PROCESS).processInstanceId
        val task = taskService.createTaskQuery()
            .processInstanceId(processInstanceId)
            .singleResult()
        taskService.complete(task.id)

        val instanceIds = camundaHistoricProcessInstanceRepository.findAll(
            CamundaHistoricProcessInstanceSpecificationHelper.byEndTimeBefore(now)
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsAll(getProcessInstancesByKey(TEST_PROCESS).map { it.id })
        Assertions.assertThat(instanceIds).doesNotContainAnyElementsOf(getProcessInstancesByKey(USER_TASK_PROCESS).map { it.id })
    }

    @Test
    @Transactional
    fun byStartUserId() {
        val processInstance = getRandomProcessInstanceByKey(USER_TASK_PROCESS)

        val instanceIds = camundaHistoricProcessInstanceRepository.findAll(
            CamundaHistoricProcessInstanceSpecificationHelper.byStartUserId("johndoe")
        ).map { it.id }

        Assertions.assertThat(instanceIds).containsExactly(processInstance.id)
    }

    private fun getRandomProcessInstance() = processInstancesMap.values.flatten().random()
    private fun getRandomProcessInstanceByKey(key: String) = processInstancesMap[key]!!.random()

    private fun getProcessInstancesByKey(key: String) = processInstancesMap[key]!!

    companion object {
        const val TEST_PROCESS = "test-process"
        const val USER_TASK_PROCESS = "user-task-process"
    }
}