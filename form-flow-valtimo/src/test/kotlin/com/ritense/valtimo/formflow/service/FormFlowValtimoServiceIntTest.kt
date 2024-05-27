/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.valtimo.formflow.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.service.FormFlowService
import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.service.ProcessLinkActivityService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper
import com.ritense.valtimo.formflow.BaseIntegrationTest
import com.ritense.valtimo.formflow.FormFlowTaskOpenResultProperties
import com.ritense.valtimo.formflow.web.rest.dto.FormFlowProcessLinkCreateRequestDto
import com.ritense.valtimo.service.CamundaTaskService
import org.camunda.bpm.engine.RepositoryService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.assertEquals

@Transactional
internal class FormFlowValtimoServiceIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var processLinkService: ProcessLinkService

    @Autowired
    lateinit var taskService: CamundaTaskService

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Autowired
    lateinit var repositoryService: RepositoryService

    @Autowired
    lateinit var processLinkActivityService: ProcessLinkActivityService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var formFlowValtimoService: FormFlowValtimoService

    @Test
    fun `should filter illegal fields from submissionData`() {
        linkFormFlowToUserTask("inkomens_loket:latest")
        val documentAndProcess = newDocumentAndStartProcess()
        val processInstanceId = documentAndProcess.resultingProcessInstanceId().get()
        val formFlowInstance = openTasks(processInstanceId).single()
        val submissionData = objectMapper.readTree("""
            {
                "IllegalField1": "test",
                "person":{"firstName":"Henk"},
                "illegalField2":{"illegalField3":5}
            }""".trimIndent())

        val filteredSubmissionData = formFlowValtimoService.getVerifiedSubmissionData(submissionData, formFlowInstance)

        assertEquals("""{"person":{"firstName":"Henk"}}""", filteredSubmissionData.toString())
    }

    private fun linkFormFlowToUserTask(formFlowDefinitionId: String) {
        processLinkService.createProcessLink(
            FormFlowProcessLinkCreateRequestDto(
                getProcessDefinitionId(),
                "do-something",
                ActivityTypeWithEventName.USER_TASK_CREATE,
                formFlowDefinitionId
            )
        )
    }

    private fun getProcessDefinitionId(): String {
        return repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("formflow-one-task-process")
            .latestVersion()
            .singleResult()
            .id
    }

    private fun newDocumentAndStartProcess(): NewDocumentAndStartProcessResult {
        return runWithoutAuthorization {
            processDocumentService.newDocumentAndStartProcess(
                NewDocumentAndStartProcessRequest(
                    "formflow-one-task-process",
                    NewDocumentRequest(
                        "profile",
                        objectMapper.readTree("{}")
                    )
                )
            )
        }
    }

    private fun openTasks(processInstanceId: ProcessInstanceId): List<FormFlowInstance> {
        return runWithoutAuthorization {
            taskService.findTasks(CamundaTaskSpecificationHelper.byProcessInstanceId(processInstanceId.toString()))
                .asSequence()
                .map { processLinkActivityService.openTask(UUID.fromString(it.id)) }
                .filter { it.properties is FormFlowTaskOpenResultProperties }
                .map { (it.properties as FormFlowTaskOpenResultProperties).formFlowInstanceId }
                .map { formFlowService.getInstanceById(FormFlowInstanceId(it)) }
                .toList()
        }
    }

}
