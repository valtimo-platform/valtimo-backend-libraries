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

package com.ritense.form.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.domain.Mapper
import com.ritense.form.domain.submission.formfield.FormField
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.form.web.rest.dto.FormSubmissionResult
import com.ritense.form.web.rest.dto.FormSubmissionResultFailed
import com.ritense.form.web.rest.dto.FormSubmissionResultSucceeded
import com.ritense.processdocument.domain.ProcessDocumentDefinition
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.request.Request
import com.ritense.processdocument.exception.ProcessDocumentDefinitionNotFoundException
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.domain.ActivityTypeWithEventName.START_EVENT_START
import com.ritense.processlink.domain.ActivityTypeWithEventName.USER_TASK_CREATE
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import com.ritense.valtimo.contract.json.patch.JsonPatch
import com.ritense.valtimo.contract.result.OperationError
import com.ritense.valtimo.contract.result.OperationError.FromException
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.service.CamundaTaskService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

open class FormSubmissionService(
    private val processLinkService: ProcessLinkService,
    private val formDefinitionService: FormIoFormDefinitionService,
    private val documentService: JsonSchemaDocumentService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val processDocumentService: ProcessDocumentService,
    private val camundaTaskService: CamundaTaskService,
    private val processService: CamundaProcessService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val prefillFormService: PrefillFormService,
) {

    @Transactional
    open fun handleSubmission(
        processLinkId: UUID,
        formData: JsonNode,
        documentId: String?,
        taskInstanceId: String?,
    ): FormSubmissionResult {
        return try {
            val processLink = processLinkService.getProcessLink(processLinkId, FormProcessLink::class.java)
            val document = documentId?.let { documentService.get(documentId) }
            val processDocumentDefinition = getProcessDocumentDefinition(processLink, document)
            val processVariables = getProcessVariables(taskInstanceId)
            val formDefinition = formDefinitionService.getFormDefinitionById(processLink.formDefinitionId).orElseThrow()
            val formFields = getFormFields(formDefinition, formData)
            val submittedDocumentContent = getSubmittedDocumentContent(formFields, document)
            val formDefinedProcessVariables = formDefinition.extractProcessVars(formData)
            val preJsonPatch = getPreJsonPatch(formDefinition, submittedDocumentContent, processVariables, document)
            val externalFormData = getExternalFormData(formDefinition, formData)
            val request = getRequest(
                processLink,
                document,
                taskInstanceId,
                processDocumentDefinition,
                submittedDocumentContent,
                formDefinedProcessVariables,
                preJsonPatch
            )
            return dispatchRequest(request, processDocumentDefinition, formFields, externalFormData)
        } catch (notFoundException: DocumentNotFoundException) {
            logger.error("Document could not be found", notFoundException)
            FormSubmissionResultFailed(FromException(notFoundException))
        } catch (notFoundException: ProcessDocumentDefinitionNotFoundException) {
            logger.error("ProcessDocumentDefinition could not be found", notFoundException)
            FormSubmissionResultFailed(FromException(notFoundException))
        } catch (ex: RuntimeException) {
            val referenceId = UUID.randomUUID()
            logger.error("Unexpected error occurred - {}", referenceId, ex)
            FormSubmissionResultFailed(
                OperationError.FromString("Unexpected error occurred, please contact support - referenceId: $referenceId")
            )
        }
    }

    private fun getProcessDocumentDefinition(
        processLink: ProcessLink,
        document: Document?
    ): ProcessDocumentDefinition {
        val processDefinition = processService.getProcessDefinitionById(processLink.processDefinitionId)
        val processDefinitionKey = CamundaProcessDefinitionKey(processDefinition.key)
        return if (document == null) {
            processDocumentAssociationService.getProcessDocumentDefinition(processDefinitionKey)
        } else {
            processDocumentAssociationService.getProcessDocumentDefinition(
                processDefinitionKey,
                document.definitionId().version()
            )
        }
    }

    private fun getProcessVariables(taskInstanceId: String?): JsonNode? {
        return if (!taskInstanceId.isNullOrEmpty()) {
            Mapper.INSTANCE.get().valueToTree(camundaTaskService.getTaskVariables(taskInstanceId))
        } else {
            null
        }
    }

    private fun getFormFields(
        formDefinition: FormIoFormDefinition,
        formData: JsonNode
    ): List<FormField> {
        return formDefinition.documentMappedFieldsForSubmission
            .mapNotNull { objectNode -> FormField.getFormField(formData, objectNode, applicationEventPublisher) }
    }

    private fun getSubmittedDocumentContent(formFields: List<FormField>, document: Document?): ObjectNode {
        val submittedDocumentContent = JsonNodeFactory.instance.objectNode()
        formFields.forEach {
            it.preProcess(document)
            it.appendValueToDocument(submittedDocumentContent)
        }
        return submittedDocumentContent
    }

    private fun getPreJsonPatch(
        formDefinition: FormIoFormDefinition,
        submittedDocumentContent: JsonNode,
        processVariables: JsonNode?,
        document: Document?
    ): JsonPatch {
        //Note: Pre patch can be refactored into a specific field types that apply itself
        val preJsonPatch = prefillFormService.preSubmissionTransform(
            formDefinition,
            submittedDocumentContent,
            processVariables ?: jacksonObjectMapper().createObjectNode(),
            document?.content()?.asJson() ?: jacksonObjectMapper().createObjectNode()
        )
        logger.debug { "getContent:$submittedDocumentContent" }
        return preJsonPatch
    }

    private fun getExternalFormData(
        formDefinition: FormIoFormDefinition,
        formData: JsonNode
    ): Map<String, Map<String, JsonNode>> {
        return formDefinition.buildExternalFormFieldsMapForSubmission().map { entry ->
            entry.key to entry.value.associate {
                it.name to formData.at(it.jsonPointer)
            }
        }.toMap()
    }

    private fun getRequest(
        processLink: FormProcessLink,
        document: Document?,
        taskInstanceId: String?,
        processDocumentDefinition: ProcessDocumentDefinition,
        submittedDocumentContent: JsonNode,
        formDefinedProcessVariables: Map<String, Any>,
        preJsonPatch: JsonPatch
    ): Request {
        return if (processLink.activityType == START_EVENT_START) {
            if (processDocumentDefinition.canInitializeDocument()) {
                newDocumentAndStartProcessRequest(
                    processDocumentDefinition,
                    submittedDocumentContent,
                    formDefinedProcessVariables
                )
            } else {
                modifyDocumentAndStartProcessRequest(
                    document!!,
                    processDocumentDefinition,
                    submittedDocumentContent,
                    formDefinedProcessVariables,
                    preJsonPatch
                )
            }
        } else if (processLink.activityType == USER_TASK_CREATE) {
            modifyDocumentAndCompleteTaskRequest(
                document!!,
                taskInstanceId!!,
                submittedDocumentContent,
                formDefinedProcessVariables,
                preJsonPatch
            )
        } else {
            throw UnsupportedOperationException("Cannot handle submission for activity-type '" + processLink.activityType + "'")
        }
    }

    private fun newDocumentAndStartProcessRequest(
        processDocumentDefinition: ProcessDocumentDefinition,
        submittedDocumentContent: JsonNode,
        formDefinedProcessVariables: Map<String, Any>,
    ): NewDocumentAndStartProcessRequest {
        return NewDocumentAndStartProcessRequest(
            processDocumentDefinition.processDocumentDefinitionId().processDefinitionKey()
                .toString(),
            NewDocumentRequest(
                processDocumentDefinition.processDocumentDefinitionId().documentDefinitionId().name(),
                submittedDocumentContent
            )
        ).withProcessVars(formDefinedProcessVariables)
    }

    private fun modifyDocumentAndStartProcessRequest(
        document: Document,
        processDocumentDefinition: ProcessDocumentDefinition,
        submittedDocumentContent: JsonNode,
        formDefinedProcessVariables: Map<String, Any>,
        preJsonPatch: JsonPatch
    ): ModifyDocumentAndStartProcessRequest {
        return ModifyDocumentAndStartProcessRequest(
            processDocumentDefinition.processDocumentDefinitionId().processDefinitionKey().toString(),
            ModifyDocumentRequest(
                document.id().toString(),
                submittedDocumentContent,
                document.version().toString()
            ).withJsonPatch(preJsonPatch)
        ).withProcessVars(formDefinedProcessVariables)
    }

    private fun modifyDocumentAndCompleteTaskRequest(
        document: Document,
        taskInstanceId: String,
        submittedDocumentContent: JsonNode,
        formDefinedProcessVariables: Map<String, Any>,
        preJsonPatch: JsonPatch
    ): ModifyDocumentAndCompleteTaskRequest {
        return ModifyDocumentAndCompleteTaskRequest(
            ModifyDocumentRequest(
                document.id().toString(),
                submittedDocumentContent,
                document.version().toString()
            ).withJsonPatch(preJsonPatch),
            taskInstanceId
        ).withProcessVars(formDefinedProcessVariables)
    }

    private fun dispatchRequest(
        request: Request,
        processDocumentDefinition: ProcessDocumentDefinition,
        formFields: List<FormField>,
        externalFormData: Map<String, Map<String, JsonNode>>,
    ): FormSubmissionResult {
        return try {
            val result = processDocumentService.dispatch(request)
            return if (result.errors().isNotEmpty()) {
                FormSubmissionResultFailed(result.errors())
            } else {
                val submittedDocument = result.resultingDocument().orElseThrow()
                formFields.forEach { it.postProcess(submittedDocument) }
                publishExternalDataSubmittedEvent(externalFormData, processDocumentDefinition, submittedDocument)
                FormSubmissionResultSucceeded(submittedDocument.id().toString())
            }
        } catch (ex: RuntimeException) {
            val referenceId = UUID.randomUUID()
            logger.error("Unexpected error occurred - $referenceId", ex)
            FormSubmissionResultFailed(
                OperationError.FromString("Unexpected error occurred, please contact support - referenceId: $referenceId")
            )
        }
    }

    private fun publishExternalDataSubmittedEvent(
        externalFormData: Map<String, Map<String, JsonNode>>,
        processDocumentDefinition: ProcessDocumentDefinition,
        submittedDocument: Document
    ) {
        if (externalFormData.isNotEmpty()) {
            applicationEventPublisher.publishEvent(
                ExternalDataSubmittedEvent(
                    externalFormData,
                    processDocumentDefinition.processDocumentDefinitionId().documentDefinitionId().name(),
                    submittedDocument.id().id
                )
            )
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
