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

package com.ritense.valtimo.formflow

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.formflow.repository.FormFlowInstanceRepository
import com.ritense.formflow.service.FormFlowService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkActivityService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.formflow.domain.FormFlowProcessLink
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkCreateRequestDto
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
internal class FormFlowProcessLinkActivityHandlerIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var formFlowInstanceRepository: FormFlowInstanceRepository

    @Autowired
    lateinit var processLinkService: ProcessLinkService

    @Autowired
    lateinit var processLinkActivityService: ProcessLinkActivityService

    @Autowired
    lateinit var camundaProcessService: CamundaProcessService

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var repositoryService: RepositoryService

    @Autowired
    lateinit var processLinkActivityHandler :FormFlowProcessLinkActivityHandler

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Test
    fun `should not create form flow instance when Camunda user task is created`() {

        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .processDefinitionKey("formflow-one-task-process")
            .singleResult();

        processLinkService.createProcessLink(
            FormFlowProcessLinkCreateRequestDto(
                processDefinitionId = processDefinition.id,
                activityId = "do-something",
                activityType = ActivityTypeWithEventName.USER_TASK_START,
                formFlowDefinitionId = "inkomens_loket:latest"
            )
        )

        runWithoutAuthorization{
            camundaProcessService.startProcess(
                processDefinition.key,
                UUID.randomUUID().toString(),
                mapOf()
            )
        }

        assertEquals(0, formFlowInstanceRepository.findAll().size)
    }

    @Test
    fun `should create form flow instance when task is opened`() {
        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .processDefinitionKey("formflow-one-task-process")
            .singleResult();

        processLinkService.createProcessLink(
            FormFlowProcessLinkCreateRequestDto(
                processDefinitionId = processDefinition.id,
                activityId = "do-something",
                activityType = ActivityTypeWithEventName.USER_TASK_START,
                formFlowDefinitionId = "inkomens_loket:latest"
            )
        )

        val processInstance = runWithoutAuthorization {
            camundaProcessService.startProcess(
                processDefinition.key,
                UUID.randomUUID().toString(),
                mapOf()
            )
        }

        val task = taskService.createTaskQuery()
            .processInstanceId(processInstance.processInstanceDto.id)
            .singleResult()

        assertEquals(0, formFlowInstanceRepository.findAll().size)

        processLinkActivityService.openTask(UUID.fromString(task.id))

        assertEquals(1, formFlowInstanceRepository.findAll().size)
    }

    @Test
    fun `should retrieve form-flow and create instance`(){
        val processLinkId = UUID.randomUUID()

        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .processDefinitionKey("formflow-one-task-process")
            .singleResult();

        val formFlowDefinition = formFlowService.findDefinition("inkomens_loket:1")

        val processLink: ProcessLink = FormFlowProcessLink(
            id = processLinkId,
            processDefinitionId = processDefinition.id,
            activityId = "some_activity_id",
            activityType = ActivityTypeWithEventName.START_EVENT_START,
            formFlowDefinitionId = formFlowDefinition?.id.toString())

        val result = processLinkActivityHandler.getStartEventObject(
            processDefinition.id,
            null,
            "some-document",
            processLink
        )
        val dbFormFlowInstances = formFlowInstanceRepository.findAll().filter { it.formFlowDefinition.id.toString() == "inkomens_loket:1" }
        assertEquals(1, dbFormFlowInstances.size)
        assertEquals("form-flow",result.type)
        assertEquals(dbFormFlowInstances[0].id.id,result.properties.formFlowInstanceId)
        val additionalProperties = dbFormFlowInstances[0].getAdditionalProperties()
        assertEquals(additionalProperties["documentDefinitionName"], "some-document")
        assertEquals(additionalProperties["processDefinitionKey"], "formflow-one-task-process")
    }
}
