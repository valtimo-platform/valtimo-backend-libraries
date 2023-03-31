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

package com.ritense.processlink.service

import com.ritense.processlink.BaseIntegrationTest
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.CustomProcessLinkCreateRequestDto
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import javax.transaction.Transactional
import kotlin.test.assertEquals

@Transactional
internal class CopyProcessLinkOnProcessDeploymentListenerIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var processLinkService: ProcessLinkService

    @Autowired
    lateinit var repositoryService: RepositoryService

    @Autowired
    lateinit var camundaProcessService: CamundaProcessService

    private lateinit var processDefinition: ProcessDefinition

    @BeforeEach
    fun beforeEach() {
        processDefinition = getLatestProcessDefinition()
    }

    @Test
    fun `should copy process link on latest process to a newly deployed process`() {
        // given
        createProcessLink(processDefinition)
        val changedProcessBpmn = readFileAsString("/bpmn/service-task-process.bpmn")
            .replace("My service task", "My service task changed")

        // when
        camundaProcessService.deploy("service-task-process.bpmn", changedProcessBpmn.byteInputStream())

        // then
        val latestProcessDefinition = getLatestProcessDefinition()
        assertEquals(1, processDefinition.version)
        assertEquals(1, processLinkService.getProcessLinks(processDefinition.id, SERVICE_TASK_ID).count())
        assertEquals(2, latestProcessDefinition.version)
        assertEquals(1, processLinkService.getProcessLinks(latestProcessDefinition.id, SERVICE_TASK_ID).count())
    }

    @Test
    fun `should NOT copy process link on old process to a newly deployed process`() {
        // given
        val changedProcessBpmn = readFileAsString("/bpmn/service-task-process.bpmn")
            .replace("My service task", "My service task changed")
        camundaProcessService.deploy("service-task-process.bpmn", changedProcessBpmn.byteInputStream())
        createProcessLink(processDefinition)
        val changedAgainProcessBpmn = readFileAsString("/bpmn/service-task-process.bpmn")
            .replace("My service task", "My service task changed again")

        // when
        camundaProcessService.deploy("service-task-process.bpmn", changedAgainProcessBpmn.byteInputStream())

        // then
        val latestProcessDefinition = getLatestProcessDefinition()
        assertEquals(1, processDefinition.version)
        assertEquals(1, processLinkService.getProcessLinks(processDefinition.id, SERVICE_TASK_ID).count())
        assertEquals(3, latestProcessDefinition.version)
        assertEquals(0, processLinkService.getProcessLinks(latestProcessDefinition.id, SERVICE_TASK_ID).count())
    }

    private fun createProcessLink(processDefinition: ProcessDefinition) {
        processLinkService.createProcessLink(
            CustomProcessLinkCreateRequestDto(
                processDefinition.id,
                SERVICE_TASK_ID,
                ActivityTypeWithEventName.SERVICE_TASK_START
            )
        )
    }

    private fun getLatestProcessDefinition(): ProcessDefinition {
        return repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .latestVersion()
            .singleResult()
    }

    private fun readFileAsString(fileName: String) = this::class.java.getResource(fileName).readText(Charsets.UTF_8)

    companion object {
        private const val PROCESS_DEFINITION_KEY = "service-task-process"
        private const val SERVICE_TASK_ID = "my-service-task"
    }
}
