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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.formflow.repository.FormFlowInstanceRepository
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType
import com.ritense.formlink.domain.request.CreateFormAssociationRequest
import com.ritense.formlink.domain.request.FormLinkRequest
import com.ritense.formlink.service.FormAssociationService
import com.ritense.formlink.service.ProcessLinkService
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byProcessInstanceId
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.service.CamundaTaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
internal class FormFlowFormLinkTaskProviderIntTest: BaseIntegrationTest() {
    @Autowired
    lateinit var documentDefinitionService: DocumentDefinitionService

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var formAssociationService: FormAssociationService

    @Autowired
    lateinit var formFlowInstanceRepository: FormFlowInstanceRepository

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var processLinkService: ProcessLinkService

    @Autowired
    lateinit var taskService: CamundaTaskService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should not create form flow instance when Camunda user task is created`() {
        runWithoutAuthorization {
            documentDefinitionService.deploy(
                "" +
                    "{\n" +
                    "    \"\$id\": \"testing.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
            )
            processDocumentAssociationService.createProcessDocumentDefinition(
                ProcessDocumentDefinitionRequest(
                    "formflow-one-task-process",
                    "testing",
                    true
                )
            )
        }

        formAssociationService.createFormAssociation(
            CreateFormAssociationRequest("formflow-one-task-process",
                FormLinkRequest(
                    "do-something",
                    FormAssociationType.USER_TASK,
                    null,
                    "inkomens_loket:latest",
                    null,
                    null
                )
            )
        )

        processDocumentService.newDocumentAndStartProcess(
            NewDocumentAndStartProcessRequest("formflow-one-task-process",
                NewDocumentRequest("testing",
                    objectMapper.readTree("{}"))
            )
        )

        assertEquals(0, formFlowInstanceRepository.findAll().size)
    }

    @Test
    @WithMockUser(username = TEST_USER, authorities = [AuthoritiesConstants.USER])
    fun `should create form flow instance when task is opened`() {
        runWithoutAuthorization {
            documentDefinitionService.deploy(
                "" +
                    "{\n" +
                    "    \"\$id\": \"testing.schema\",\n" +
                    "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
                    "}\n"
            )

            processDocumentAssociationService.createProcessDocumentDefinition(
                ProcessDocumentDefinitionRequest(
                    "formflow-one-task-process",
                    "testing",
                    true
                )
            )
        }

        formAssociationService.createFormAssociation(
            CreateFormAssociationRequest(
                "formflow-one-task-process",
                FormLinkRequest(
                    "do-something",
                    FormAssociationType.USER_TASK,
                    null,
                    "inkomens_loket:latest",
                    null,
                    null
                )
            )
        )

        val result = runWithoutAuthorization {
            processDocumentService.newDocumentAndStartProcess(
                NewDocumentAndStartProcessRequest(
                    "formflow-one-task-process",
                    NewDocumentRequest(
                        "testing",
                        objectMapper.readTree("{}")
                    )
                )
            )
        }

        val task = taskService.findTask(byProcessInstanceId(result.resultingProcessInstanceId().get().toString()))
        assertEquals(0, formFlowInstanceRepository.findAll().size)

        processLinkService.openTask(UUID.fromString(task.id))

        assertEquals(1, formFlowInstanceRepository.findAll().size)
    }

    companion object {
        private const val TEST_USER = "user@valtimo.nl"
    }
}
