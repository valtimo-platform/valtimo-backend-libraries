package com.ritense.valtimo.formflow.handler

import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.formflow.repository.FormFlowInstanceRepository
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType
import com.ritense.formlink.domain.request.CreateFormAssociationRequest
import com.ritense.formlink.domain.request.FormLinkRequest
import com.ritense.formlink.service.FormAssociationService
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.formflow.BaseIntegrationTest
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class FormFlowCreateTaskEventHandlerIntTest: BaseIntegrationTest() {
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
    lateinit var taskService: TaskService

    @Test
    fun `create form flow instance`() {
        documentDefinitionService.deploy("" +
            "{\n" +
            "    \"\$id\": \"testing.schema\",\n" +
            "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\"\n" +
            "}\n")

        processDocumentAssociationService.createProcessDocumentDefinition(
            ProcessDocumentDefinitionRequest(
                "one-task-process",
                "testing",
                true
            )
        )

        formAssociationService.createFormAssociation(
            CreateFormAssociationRequest("one-task-process",
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
            NewDocumentAndStartProcessRequest("one-task-process",
                NewDocumentRequest("testing",
                    Mapper.INSTANCE.get().readTree("{}"))
            )
        )

        assertEquals(1, formFlowInstanceRepository.findAll().size)
    }
}
