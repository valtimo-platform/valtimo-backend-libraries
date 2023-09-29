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

package com.ritense.valtimo.camunda.repository

import com.ritense.valtimo.BaseIntegrationTest
import java.util.UUID
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class CamundaTaskRepositoryIntTest @Autowired constructor(
    private val taskService: TaskService,
    private val camundaTaskRepository: CamundaTaskRepository,
): BaseIntegrationTest() {

    @Test
    @Transactional
    fun `should find camunda task instance`() {
        val instance = runtimeService.startProcessInstanceByKey(
            "one-task-process",
            UUID.randomUUID().toString()
        )

        //get the task
        val result = camundaTaskRepository.findOne { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<String>("processInstance").get<Any>("id"),
                instance.id
            )
        }
        Assertions.assertThat(result.isPresent).isTrue()

        val task = result.get()
        Assertions.assertThat(task.getProcessInstanceId()).isEqualTo(instance.id)
        Assertions.assertThat(task.taskDefinitionKey).isEqualTo("do-something")
    }

    @Test
    @Transactional
    fun `should find camunda task variables`() {
        val processInstanceVariableMap = mapOf(
            "myBoolean" to true,
            "myNumber" to 1337,
            "myText" to "Hello World!"
        )
        val instance = runtimeService.startProcessInstanceByKey(
            "one-task-process",
            UUID.randomUUID().toString(),
            processInstanceVariableMap
        )

        val nativeTask = taskService.createTaskQuery()
            .processInstanceIdIn(instance.id)
            .taskDefinitionKey("do-something")
            .singleResult()

        val localVarMap = mapOf(
            "taskBoolean" to true,
            "taskNumber" to 420,
            "taskText" to "Hello task!",
            "myText" to "Hello!"
        )
        taskService.setVariablesLocal(nativeTask.id, localVarMap)

        //get the task
        val result = camundaTaskRepository.findOne { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<String>("processInstance").get<Any>("id"),
                instance.id
            )
        }

        Assertions.assertThat(result.isPresent).isTrue()

        val task = result.get()
        Assertions.assertThat(task.getProcessInstanceId()).isEqualTo(instance.id)
        Assertions.assertThat(task.taskDefinitionKey).isEqualTo("do-something")

        //Test local variables
        localVarMap.forEach { (key , value) ->
            val variableInstance = task.variableInstances.first { it.name == key }
            Assertions.assertThat(variableInstance.getValue()).describedAs(key).isEqualTo(value)

            val localVariable = task.getVariableLocal(key)
            Assertions.assertThat(localVariable).describedAs(key).isEqualTo(value)

            val variable = task.getVariableLocal(key)
            Assertions.assertThat(variable).describedAs(key).isEqualTo(value)
        }
        val localVarNames = task.variableNamesLocal
        Assertions.assertThat(localVarNames).containsOnly(*localVarMap.keys.toTypedArray())

        //Test inherited variables from parent(s)
        val allVars = processInstanceVariableMap + localVarMap
        allVars.forEach { (key , value) ->
            val variable = task.getVariable(key)
            Assertions.assertThat(variable).describedAs(key).isEqualTo(value)
        }

        // Make sure we get the task variable value for 'myText' instead of the process instance one
        Assertions.assertThat(task.getVariable("myText")).describedAs("myText").isEqualTo("Hello!")

        val variableNames = task.variableNames
        Assertions.assertThat(variableNames).containsOnly(*allVars.keys.toTypedArray())
    }
}