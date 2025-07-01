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

package com.ritense.formflow

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.formflow.repository.FormFlowInstanceRepository
import com.ritense.formflow.service.FormFlowService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkActivityService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.operaton.repository.OperatonTaskSpecificationHelper.Companion.byProcessInstanceId
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.formflow.domain.FormFlowProcessLink
import com.ritense.formflow.web.rest.dto.FormFlowProcessLinkCreateRequestDto
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.service.OperatonProcessService
import com.ritense.valtimo.service.OperatonTaskService
import java.util.UUID
import org.operaton.bpm.engine.RepositoryService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class FormFlowProcessLinkActivityHandlerIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var formFlowInstanceRepository: FormFlowInstanceRepository

    @Autowired
    lateinit var processLinkService: ProcessLinkService

    @Autowired
    lateinit var processLinkActivityService: ProcessLinkActivityService

    @Autowired
    lateinit var operatonProcessService: OperatonProcessService

    @Autowired
    lateinit var taskService: OperatonTaskService

    @Autowired
    lateinit var repositoryService: RepositoryService

    @Autowired
    lateinit var processLinkActivityHandler :FormFlowProcessLinkActivityHandler

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Test
    fun `should not create form flow instance when Camunda user task is created`() {
        val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .processDefinitionKey("formflow-one-task-process")
            .singleResult()

        processLinkService.createProcessLink(
            FormFlowProcessLinkCreateRequestDto(
                processDefinitionId = processDefinition.id,
                activityId = "do-something",
                activityType = ActivityTypeWithEventName.USER_TASK_START,
                formFlowDefinitionKey = "inkomens_loket_alternate"
            ),
            caseDefinitionId
        )

        runWithoutAuthorization{
            operatonProcessService.startProcess(
                processDefinition.key,
                UUID.randomUUID().toString(),
                mapOf()
            )
        }

        assertEquals(0, formFlowInstanceRepository.findAll().size)
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = [USER])
    fun `should create form flow instance when task is opened`() {
        val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .processDefinitionKey("formflow-one-task-process")
            .singleResult()

        processLinkService.createProcessLink(
            FormFlowProcessLinkCreateRequestDto(
                processDefinitionId = processDefinition.id,
                activityId = "do-something",
                activityType = ActivityTypeWithEventName.USER_TASK_START,
                formFlowDefinitionKey = "inkomens_loket_alternate"
            ),
            caseDefinitionId
        )

        val processInstance = runWithoutAuthorization {
            operatonProcessService.startProcess(
                processDefinition.key,
                UUID.randomUUID().toString(),
                mapOf()
            )
        }

        val task = taskService.findTask(byProcessInstanceId(processInstance.processInstanceDto.id))

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
            .singleResult()

        val formFlowDefinition = formFlowService.findDefinition("inkomens_loket_alternate", CaseDefinitionId("profile", "1.0.0"))

        val processLink: ProcessLink = FormFlowProcessLink(
            id = processLinkId,
            processDefinitionId = processDefinition.id,
            activityId = "some_activity_id",
            activityType = ActivityTypeWithEventName.START_EVENT_START,
            formFlowDefinitionKey = formFlowDefinition?.id?.key!!)

        val result = processLinkActivityHandler.getStartEventObject(
            processDefinition.id,
            null,
            "some-document",
            processLink
        )
        val dbFormFlowInstances = formFlowInstanceRepository.findAll().filter { it.formFlowDefinition.id.toString() == "inkomens_loket_alternate" }
        assertEquals(1, dbFormFlowInstances.size)
        assertEquals("form-flow",result.type)
        assertEquals(dbFormFlowInstances[0].id.id,result.properties.formFlowInstanceId)
        val additionalProperties = dbFormFlowInstances[0].getAdditionalProperties()
        assertEquals(additionalProperties["documentDefinitionName"], "some-document")
        assertEquals(additionalProperties["processDefinitionKey"], "formflow-one-task-process")
    }

    companion object {
        private const val TEST_USER = "user@valtimo.nl"
    }
}
