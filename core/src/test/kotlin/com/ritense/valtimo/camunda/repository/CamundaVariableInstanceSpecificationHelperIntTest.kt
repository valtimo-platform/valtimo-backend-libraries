package com.ritense.valtimo.camunda.repository

import com.ritense.valtimo.BaseIntegrationTest
import java.util.UUID
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class CamundaVariableInstanceSpecificationHelperIntTest @Autowired constructor(
    private val camundaVariableInstanceRepository: CamundaVariableInstanceRepository
) : BaseIntegrationTest() {


    private lateinit var processInstances: List<ProcessInstance>

    @BeforeEach
    fun prepare() {
        processInstances = (1..3)
            .map {
                runtimeService.startProcessInstanceByKey(
                    USER_TASK_PROCESS,
                    UUID.randomUUID().toString(),
                    mapOf("someRandomVariable-$it" to "someRandomValue-$it")
                )
            }.toList()
    }

    @Test
    @Transactional
    fun byName() {
        val name = "someRandomVariable-2"
        val variableInstance = runtimeService.createVariableInstanceQuery()
            .variableName(name)
            .singleResult()

        val result = camundaVariableInstanceRepository.findOne(
            CamundaVariableInstanceSpecificationHelper.byName(name)
        ).get()

        Assertions.assertThat(result.id).isEqualTo(variableInstance.id)
        Assertions.assertThat(result.getValue()).isEqualTo("someRandomValue-2")
    }

    @Test
    @Transactional
    fun byNameIn() {
        val names = (1..2).map { "someRandomVariable-$it" }.toTypedArray()
        val variableInstanceIds = runtimeService.createVariableInstanceQuery()
            .variableNameIn(*names)
            .list().map { it.id }

        val resultIds = camundaVariableInstanceRepository.findAll(
            CamundaVariableInstanceSpecificationHelper.byNameIn(*names)
        ).map { it.id }

        Assertions.assertThat(resultIds).containsExactlyInAnyOrderElementsOf(variableInstanceIds)
    }

    @Test
    @Transactional
    fun byProcessInstanceId() {
        val processInstanceId = processInstances.random().id
        val variableInstanceIds = runtimeService.createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .list().map { it.id }

        val resultIds = camundaVariableInstanceRepository.findAll(
            CamundaVariableInstanceSpecificationHelper.byProcessInstanceId(processInstanceId)
        ).map { it.id }

        Assertions.assertThat(resultIds).containsExactlyInAnyOrderElementsOf(variableInstanceIds)
    }

    @Test
    @Transactional
    fun byProcessInstanceIdIn() {
        val processInstanceIds = listOf(processInstances[1], processInstances[2]).map { it.id }.toTypedArray()
        val variableInstanceIds = runtimeService.createVariableInstanceQuery()
            .processInstanceIdIn(*processInstanceIds)
            .list().map { it.id }

        val resultIds = camundaVariableInstanceRepository.findAll(
            CamundaVariableInstanceSpecificationHelper.byProcessInstanceIdIn(*processInstanceIds)
        ).map { it.id }

        Assertions.assertThat(resultIds).containsExactlyInAnyOrderElementsOf(variableInstanceIds)

    }

    companion object {
        const val USER_TASK_PROCESS = "user-task-process"
    }
}