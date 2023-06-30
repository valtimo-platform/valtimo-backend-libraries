package com.ritense.valtimo.camunda.repository

import com.ritense.valtimo.BaseIntegrationTest
import java.time.LocalDateTime
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class CamundaTaskSpecificationHelperTest @Autowired constructor(
    private val taskService: TaskService,
    private val camundaTaskRepository: CamundaTaskRepository
): BaseIntegrationTest() {

    var oneTaskInstances: List<ProcessInstance>? = null
    var userTaskInstance: ProcessInstance? = null
    var createDate: LocalDateTime? = null

    @BeforeEach
    fun prepare() {
        createDate = LocalDateTime.now()

        oneTaskInstances = (1..3)
            .map {
                runtimeService.startProcessInstanceByKey(
                    "one-task-process",
                    UUID.randomUUID().toString(),
                    mapOf("oneTask" to true)
                )
            }.toList()

        userTaskInstance = runtimeService.startProcessInstanceByKey(
            "user-task-process",
            UUID.randomUUID().toString(),
            mapOf("userTask" to true)
        )
    }


    @Test
    @Transactional
    fun byId() {
        val taskId = getRandomOneTaskProcessTask().id

        val camundaTask = camundaTaskRepository.findOne(CamundaTaskSpecificationHelper.byId(taskId)).get()

        assertThat(camundaTask.id).isEqualTo(taskId)
    }

    @Test
    @Transactional
    fun byProcessInstanceId() {
        val processInstanceId = getRandomOneTaskProcessInstance().id
        val camundaTask = camundaTaskRepository.findOne(CamundaTaskSpecificationHelper.byProcessInstanceId(processInstanceId)).get()

        assertThat(camundaTask.getProcessInstanceId()).isEqualTo(processInstanceId)
    }

    @Test
    @Transactional
    fun byProcessInstanceBusinessKey() {
        val processInstance = getRandomOneTaskProcessInstance()
        val camundaTask = camundaTaskRepository.findOne(CamundaTaskSpecificationHelper.byProcessInstanceBusinessKey(processInstance.businessKey)).get()

        assertThat(camundaTask.getProcessInstanceId()).isEqualTo(processInstance.id)
    }

    @Test
    @Transactional
    fun byProcessDefinitionKeys() {
        val allProcessInstanceIds = (oneTaskInstances!! + userTaskInstance!!).map { it.processInstanceId }

        val camundaTasksProcessInstanceIds = camundaTaskRepository.findAll(CamundaTaskSpecificationHelper.byProcessDefinitionKeys(setOf("one-task-process", "user-task-process")))
            .map { it.getProcessInstanceId() }

        assertThat(camundaTasksProcessInstanceIds).containsAll(allProcessInstanceIds)
    }

    @Test
    @Transactional
    fun byProcessDefinitionId() {
        val allOneTaskIds = taskService.createTaskQuery()
            .taskDefinitionKey("one-task-process")
            .list()
            .map { it.id }

        val camundaTasksIds = camundaTaskRepository.findAll(CamundaTaskSpecificationHelper.byProcessDefinitionId(getRandomOneTaskProcessInstance().processDefinitionId))
            .map { it.id }

        assertThat(camundaTasksIds).containsAll(allOneTaskIds)
    }

    @Test
    @Transactional
    fun byCandidateGroups() {
        val allTaskIds = getAllTaskIds()

        val camundaTasksIds = camundaTaskRepository.findAll(CamundaTaskSpecificationHelper.byCandidateGroups("ROLE_USER", "ROLE_ADMIN"))
            .map { it.id }

        assertThat(camundaTasksIds).containsAll(allTaskIds)
    }

    @Test
    @Transactional
    fun byName() {
        val allOneTaskIds = taskService.createTaskQuery()
            .taskDefinitionKey("one-task-process")
            .list()
            .map { it.id }

        val camundaTasksIds = camundaTaskRepository.findAll(CamundaTaskSpecificationHelper.byName("Do something"))
            .map { it.id }

        assertThat(camundaTasksIds).containsAll(allOneTaskIds)
    }

    @Test
    @Transactional
    fun byAssignee() {
        val randomUserId = UUID.randomUUID().toString()
        val randomTask = getRandomOneTaskProcessTask()

        taskService.setAssignee(randomTask.id, randomUserId)

        val camundaTask = camundaTaskRepository.findOne(CamundaTaskSpecificationHelper.byAssignee(randomUserId)).get()

        assertThat(camundaTask.id).isEqualTo(randomTask.id)
    }

    @Test
    @Transactional
    fun byUnassigned() {
        val randomTask = getRandomOneTaskProcessTask()

        taskService.setAssignee(randomTask.id, UUID.randomUUID().toString())

        val camundaTaskIds = camundaTaskRepository.findAll(CamundaTaskSpecificationHelper.byUnassigned())
            .map { it.id }

        val allTaskIds = getAllTaskIds()

        assertThat(camundaTaskIds).hasSize(allTaskIds.size - 1)
        assertThat(camundaTaskIds).doesNotContain(randomTask.id)
    }

    @Test
    @Transactional
    fun byAssigned() {
        val randomTask = getRandomOneTaskProcessTask()

        taskService.setAssignee(randomTask.id, UUID.randomUUID().toString())

        val camundaTask = camundaTaskRepository.findOne(CamundaTaskSpecificationHelper.byAssigned()).get()

        assertThat(camundaTask.id).isEqualTo(randomTask.id)
    }

    @Test
    @Transactional
    fun byCreateTimeAfter() {
        val camundaTaskIds = camundaTaskRepository.findAll(CamundaTaskSpecificationHelper.byCreateTimeAfter(createDate!!))
            .map { it.id }

        val allTaskIds = getAllTaskIds()

        assertThat(camundaTaskIds).containsAll(allTaskIds)
    }

    @Test
    @Transactional
    fun byCreateTimeBefore() {
        val camundaTaskIds = camundaTaskRepository.findAll(CamundaTaskSpecificationHelper.byCreateTimeAfter(LocalDateTime.now()))
            .map { it.id }

        val allTaskIds = getAllTaskIds()

        assertThat(camundaTaskIds).containsAll(allTaskIds)
    }

    @Test
    @Transactional
    fun bySuspensionState() {
        val task = getRandomOneTaskProcessTask()
        taskService.complete(task.id)

        val camundaTaskIds = camundaTaskRepository.findAll(
            CamundaTaskSpecificationHelper.bySuspensionState(
                SuspensionState.ACTIVE.stateCode)
            )
            .map { it.id }

        assertThat(camundaTaskIds).isNotEmpty
        assertThat(camundaTaskIds).doesNotContain(task.id)
    }

    @Test
    @Disabled
    @Transactional
    fun byActive() {
        val task = getRandomOneTaskProcessTask()
        taskService.complete(task.id)

        val camundaTaskIds = camundaTaskRepository.findAll(
            CamundaTaskSpecificationHelper.byActive())
            .map { it.id }

        assertThat(camundaTaskIds).isNotEmpty
        assertThat(camundaTaskIds).doesNotContain(task.id)
    }

    private fun getAllTaskIds(): List<String> {
        return taskService.createTaskQuery()
            .list()
            .map { it.id }
    }

    private fun getRandomOneTaskProcessInstance() = oneTaskInstances!!.random()

    private fun getRandomOneTaskProcessTask() = taskService.createTaskQuery()
        .processInstanceId(getRandomOneTaskProcessInstance().id)
        .singleResult()
}