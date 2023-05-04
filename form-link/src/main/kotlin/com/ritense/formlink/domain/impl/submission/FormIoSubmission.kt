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

package com.ritense.formlink.domain.impl.submission

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.impl.formassociation.Mapper
import com.ritense.formlink.domain.impl.formassociation.StartEventFormAssociation
import com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation
import com.ritense.formlink.domain.impl.submission.formfield.FormField
import com.ritense.formlink.service.SubmissionTransformerService
import com.ritense.formlink.service.impl.result.FormSubmissionResultFailed
import com.ritense.formlink.service.impl.result.FormSubmissionResultSucceeded
import com.ritense.formlink.service.result.FormSubmissionResult
import com.ritense.processdocument.domain.ProcessDocumentDefinition
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.request.Request
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import com.ritense.valtimo.contract.json.patch.JsonPatch
import com.ritense.valtimo.contract.result.OperationError
import com.ritense.valtimo.contract.result.OperationError.FromString
import com.ritense.valtimo.service.CamundaTaskService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import java.util.UUID
import java.util.function.Consumer

@Deprecated("Since 10.6.0", ReplaceWith("com.ritense.form.service.FormSubmissionService"))
data class FormIoSubmission(
    val formAssociation: FormAssociation,
    val formDefinition: FormIoFormDefinition,
    val processDocumentDefinition: ProcessDocumentDefinition,
    val formData: JsonNode,
    var document: JsonSchemaDocument?,
    val taskInstanceId: String?,
    val processDocumentService: ProcessDocumentService,
    val taskService: CamundaTaskService,
    val submissionTransformerService: SubmissionTransformerService<FormIoFormDefinition>,
    val applicationEventPublisher: ApplicationEventPublisher
) : Submission {

    private val logger = KotlinLogging.logger {}
    private val documentContent: ObjectNode = emptyContent()
    private var processVariables: JsonNode? = null
    private var formDefinedProcessVariables: Map<String, Any?>? = null
    private var documentFieldReferences: MutableList<DocumentFieldReference> = mutableListOf()
    private var preJsonPatch: JsonPatch? = null
    private val request: Request
    private lateinit var externalFormData: Map<String, Map<String, Any>>

    init {
        initDocumentDefinitionFieldReferences()  //Load all mappable document definition form fields
        initProcessVariables() //Load task related processVars
        getDocumentContent() //Sanitize and process field types
        getFormDefinedProcessVariables() //Extracting specific form configuration
        buildExternalFormData()
        request = makeRequest(this)
    }

    override fun apply(): FormSubmissionResult {
        return try {
            val result = processDocumentService.dispatch(request)
            return if (result.errors().isNotEmpty()) {
                FormSubmissionResultFailed(result.errors())
            } else {
                val submittedDocument = result.resultingDocument().orElseThrow()
                document = submittedDocument

                documentFieldReferences.forEach {
                    it.formfield.postProcess()
                }
                if (externalFormData.isNotEmpty()) {
                    applicationEventPublisher.publishEvent(
                        ExternalDataSubmittedEvent(
                            externalFormData,
                            processDocumentDefinition.processDocumentDefinitionId().documentDefinitionId().name(),
                            submittedDocument.id().id
                        )
                    )
                }
                FormSubmissionResultSucceeded(submittedDocument.id().toString())
            }
        } catch (ex: RuntimeException) {
            FormSubmissionResultFailed(parseAndLogException(ex))
        }
    }

    fun documentContent(): ObjectNode {
        return documentContent
    }

    private fun initDocumentDefinitionFieldReferences() {
        formDefinition.documentMappedFieldsForSubmission.forEach(Consumer { objectNode: ObjectNode ->
            val formField = FormField.getFormField(formData, objectNode, { document }, applicationEventPublisher)
            if (formField != null) {
                documentFieldReferences.add(
                    DocumentFieldReference(formField)
                )
            }
        })
    }

    private fun initProcessVariables() {
        if (!taskInstanceId.isNullOrEmpty()) {
            val variables = taskService.getTaskVariables(taskInstanceId)
            processVariables = Mapper.INSTANCE.objectMapper().valueToTree(variables)
        }
    }

    private fun getDocumentContent(): JsonNode {
        buildDocumentContent()
        //Note: Pre patch can be refactored into a specific field types that apply itself
        preJsonPatch = submissionTransformerService.preSubmissionTransform(
            formDefinition,
            documentContent,
            processVariables,
            document?.content()?.asJson()
        )
        logger.debug { "getContent:$documentContent" }
        return documentContent
    }

    private fun getFormDefinedProcessVariables() {
        formDefinedProcessVariables = formDefinition.extractProcessVars(formData)
    }

    private fun buildExternalFormData() {
        externalFormData = formDefinition.buildExternalFormFieldsMapForSubmission().map { entry ->
            entry.key to entry.value.map {
                it.name to FormField.getValue(formData, it.jsonPointer)
            }.toMap()
        }.toMap()
    }

    private fun buildDocumentContent() {
        documentFieldReferences.map(DocumentFieldReference::formfield)
            .forEach {
                it.preProcess()
                it.appendValueToDocument(documentContent)
            }
    }

    private fun parseAndLogException(ex: Exception): OperationError {
        val referenceId = UUID.randomUUID()
        logger.error("Unexpected error occurred - $referenceId", ex)
        return FromString("Unexpected error occurred, please contact support - referenceId: $referenceId")
    }

    companion object RequestFactory {
        fun makeRequest(submission: FormIoSubmission): Request {
            if (submission.formAssociation is StartEventFormAssociation) {
                if (submission.processDocumentDefinition.canInitializeDocument()) {
                    val documentDefinitionId = submission.processDocumentDefinition.processDocumentDefinitionId().documentDefinitionId()
                    return NewDocumentAndStartProcessRequest(
                        submission.processDocumentDefinition.processDocumentDefinitionId().processDefinitionKey().toString(),
                        NewDocumentRequest(
                            documentDefinitionId.name(),
                            submission.documentContent
                        )
                    ).withProcessVars(submission.formDefinedProcessVariables)
                } else {
                    return ModifyDocumentAndStartProcessRequest(
                        submission.processDocumentDefinition.processDocumentDefinitionId().processDefinitionKey().toString(),
                        ModifyDocumentRequest(
                            submission.document?.id().toString(),
                            submission.documentContent,
                            submission.document?.version().toString()
                        ).withJsonPatch(submission.preJsonPatch)
                    ).withProcessVars(submission.formDefinedProcessVariables)
                }
            } else if (submission.formAssociation is UserTaskFormAssociation) {
                return ModifyDocumentAndCompleteTaskRequest(
                    ModifyDocumentRequest(
                        submission.document?.id().toString(),
                        submission.documentContent,
                        submission.document?.version().toString()
                    ).withJsonPatch(submission.preJsonPatch),
                    submission.taskInstanceId
                ).withProcessVars(submission.formDefinedProcessVariables)
            }
            throw UnsupportedOperationException("Cannot create request type")
        }

        fun emptyContent(): ObjectNode {
            return JsonNodeFactory.instance.objectNode()
        }
    }

}
