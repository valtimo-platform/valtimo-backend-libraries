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

package com.ritense.form.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.domain.Mapper
import com.ritense.form.domain.submission.formfield.FormField
import com.ritense.form.service.FormSubmissionService
import com.ritense.form.service.PrefillFormService
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
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.Companion.COMPLETE
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import com.ritense.valtimo.contract.json.JsonMerger
import com.ritense.valtimo.contract.json.patch.JsonPatch
import com.ritense.valtimo.contract.result.OperationError
import com.ritense.valtimo.contract.result.OperationError.FromException
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.valueresolver.ValueResolverServiceImpl
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional
import com.ritense.processdocument.resolver.DocumentJsonValueResolverFactory.Companion.PREFIX as DOC_PREFIX
import com.ritense.valueresolver.ProcessVariableValueResolverFactory.Companion.PREFIX as PV_PREFIX

open class DefaultFormSubmissionService(
    private val processLinkService: ProcessLinkService,
    private val formDefinitionService: FormIoFormDefinitionService,
    private val documentService: JsonSchemaDocumentService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val processDocumentService: ProcessDocumentService,
    private val camundaTaskService: CamundaTaskService,
    private val repositoryService: CamundaRepositoryService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val prefillFormService: PrefillFormService,
    private val authorizationService: AuthorizationService,
    private val valueResolverService: ValueResolverService
) : FormSubmissionService {

    @Transactional
    override fun handleSubmission(
        processLinkId: UUID,
        formData: JsonNode,
        documentDefinitionName: String?,
        documentId: String?,
        taskInstanceId: String?,
    ): FormSubmissionResult {
        return try {
            // TODO: Implement else, done by verifying what the processLink contains
            requireCompleteTaskPermission(taskInstanceId)

            val processLink = processLinkService.getProcessLink(processLinkId, FormProcessLink::class.java)
            val document = documentId
                ?.let { runWithoutAuthorization { documentService.get(documentId) } }
            val processDefinition = getProcessDefinition(processLink)
            val documentDefinitionNameToUse = document?.definitionId()?.name()
                ?: documentDefinitionName
                ?: getProcessDocumentDefinition(processDefinition, document).processDocumentDefinitionId()
                    .documentDefinitionId().name()
            val processVariables = getProcessVariables(taskInstanceId)
            val formDefinition = formDefinitionService.getFormDefinitionById(processLink.formDefinitionId).orElseThrow()

            val categorizedKeyValues = getCategorizedSubmitValues(formDefinition, formData)
            val formFields = getFormFields(formDefinition, formData)
            // Merge the document results from 'legacy' mapping and value-resolvers.
            val submittedDocumentContent = JsonMerger.merge(
                getSubmittedDocumentContent(formFields, document),
                categorizedKeyValues.documentValues
            )

            // Merge the process-variable results from 'legacy' mapping and value-resolvers.
            val formDefinedProcessVariables = formDefinition.extractProcessVars(formData) +
                categorizedKeyValues.processVariables

            val preJsonPatch = getPreJsonPatch(formDefinition, submittedDocumentContent, processVariables, document)
            val request = getRequest(
                processLink,
                document,
                taskInstanceId,
                documentDefinitionNameToUse,
                processDefinition.key,
                submittedDocumentContent,
                formDefinedProcessVariables,
                preJsonPatch
            )

            val externalFormData = getExternalFormData(formDefinition, formData)
            return dispatchRequest(
                request,
                formFields,
                externalFormData,
                documentDefinitionNameToUse,
                categorizedKeyValues.otherValues
            )
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

    private fun requireCompleteTaskPermission(taskInstanceId: String?) {
        if (taskInstanceId != null) {
            camundaTaskService.findTaskById(taskInstanceId)
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    CamundaTask::class.java,
                    COMPLETE,
                    camundaTaskService.findTaskById(taskInstanceId)
                )
            )
        }
    }

    /**
     * This method categorizes the submitted values which are processed by the value-resolvers.
     * It preprocesses the values for document and process-variables, and leaves the rest as-is.
     */
    private fun getCategorizedSubmitValues(
        formDefinition: FormIoFormDefinition,
        formData: JsonNode
    ): CategorizedSubmitValues {
        val categorizedMap = formDefinition.inputFields
            .mapNotNull { field ->
                getSourceKeyValuePair(field, formData)
            }.groupBy { (key, _) ->
                val prefix = key.substringBefore(ValueResolverServiceImpl.DELIMITER, missingDelimiterValue = "")
                when (prefix) {
                    DOC_PREFIX -> DOC_PREFIX
                    PV_PREFIX -> PV_PREFIX
                    else -> OTHER
                }
            }.mapValues { it.value.toMap() }

        // Preprocess the document paths & values. The result is an ObjectNode.
        val documentValues = categorizedMap[DOC_PREFIX]
            ?.let { valueResolverService.preProcessValuesForNewCase(it)[DOC_PREFIX] as? ObjectNode }
            ?: Mapper.INSTANCE.get().createObjectNode()

        // After pre-processing process-variables we have a key-value map where the prefix is stripped from the keys.
        val processVariables = categorizedMap[PV_PREFIX]
            ?.let { valueResolverService.preProcessValuesForNewCase(it)[PV_PREFIX] as? Map<String, Any>}
            ?: mapOf()

        // Do not process/handle other values yet.
        // This has to be done when we are certain the process and document could be created.
        val otherValues = categorizedMap[OTHER] ?: mapOf()

        return CategorizedSubmitValues(
            documentValues,
            processVariables,
            otherValues
        )
    }

    private fun getSourceKeyValuePair(
        field: ObjectNode,
        formData: JsonNode
    ): Pair<String, Any>? {
        return FormIoFormDefinition.GET_SOURCE_KEY.apply(field).getOrNull()?.let { sourceKey ->
            FormIoFormDefinition.GET_KEY.apply(field).getOrNull()?.let { inputKey ->
                convertNodeValue(formData.at("/$inputKey"))
            }?.let { value ->
                Pair(sourceKey, value)
            }
        }
    }

    private fun convertNodeValue(node: JsonNode): Any? {
        if (node.isMissingNode) {
            return null
        }

        return Mapper.INSTANCE.get().treeToValue<Any>(node)
    }

    private fun getProcessDefinition(
        processLink: ProcessLink
    ): CamundaProcessDefinition {
        return runWithoutAuthorization {
            repositoryService.findProcessDefinitionById(processLink.processDefinitionId)!!
        }
    }

    private fun getProcessDocumentDefinition(
        processDefinition: CamundaProcessDefinition,
        document: Document?
    ): ProcessDocumentDefinition {
        val processDefinitionKey = CamundaProcessDefinitionKey(processDefinition.key)
        return runWithoutAuthorization {
            if (document == null) {
                processDocumentAssociationService.getProcessDocumentDefinition(processDefinitionKey)
            } else {
                processDocumentAssociationService.getProcessDocumentDefinition(
                    processDefinitionKey,
                    document.definitionId().version()
                )
            }
        }
    }

    private fun getProcessVariables(taskInstanceId: String?): JsonNode? {
        return if (!taskInstanceId.isNullOrEmpty()) {
            Mapper.INSTANCE.get().valueToTree(camundaTaskService.getVariables(taskInstanceId))
        } else {
            null
        }
    }

    private fun getFormFields(
        formDefinition: FormIoFormDefinition,
        formData: JsonNode
    ): List<FormField> {
        return formDefinition.getDocumentMappedFieldsFiltered(
            FormIoFormDefinition.NOT_IGNORED.and { t -> FormIoFormDefinition.GET_SOURCE_KEY.apply(t).isEmpty }
        ).mapNotNull { objectNode -> FormField.getFormField(formData, objectNode, applicationEventPublisher) }
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
        return formDefinition.buildExternalFormFieldsMapFiltered(
            FormIoFormDefinition.NOT_IGNORED.and { t -> FormIoFormDefinition.GET_SOURCE_KEY.apply(t).isEmpty }
        ).map { entry ->
            entry.key to entry.value.associate {
                it.name to formData.at(it.jsonPointer)
            }
        }.toMap()
    }

    private fun getRequest(
        processLink: FormProcessLink,
        document: Document?,
        taskInstanceId: String?,
        documentDefinitionName: String,
        processDefinitionKey: String,
        submittedDocumentContent: JsonNode,
        formDefinedProcessVariables: Map<String, Any>,
        preJsonPatch: JsonPatch
    ): Request {
        return if (processLink.activityType == START_EVENT_START) {
            if (document == null) {
                newDocumentAndStartProcessRequest(
                    documentDefinitionName,
                    processDefinitionKey,
                    submittedDocumentContent,
                    formDefinedProcessVariables
                )
            } else {
                modifyDocumentAndStartProcessRequest(
                    document,
                    processDefinitionKey,
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
        documentDefinitionName: String,
        processDefinitionKey: String,
        submittedDocumentContent: JsonNode,
        formDefinedProcessVariables: Map<String, Any>,
    ): NewDocumentAndStartProcessRequest {
        return NewDocumentAndStartProcessRequest(
            processDefinitionKey,
            NewDocumentRequest(
                documentDefinitionName,
                submittedDocumentContent
            )
        ).withProcessVars(formDefinedProcessVariables)
    }

    private fun modifyDocumentAndStartProcessRequest(
        document: Document,
        processDefinitionKey: String,
        submittedDocumentContent: JsonNode,
        formDefinedProcessVariables: Map<String, Any>,
        preJsonPatch: JsonPatch
    ): ModifyDocumentAndStartProcessRequest {
        return ModifyDocumentAndStartProcessRequest(
            processDefinitionKey,
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
        formFields: List<FormField>,
        externalFormData: Map<String, Map<String, JsonNode>>,
        documentDefinitionName: String,
        remainingValueResolverValues: Map<String, Any>,
    ): FormSubmissionResult {
        return try {
            val result = processDocumentService.dispatch(request)
            return if (result.errors().isNotEmpty()) {
                FormSubmissionResultFailed(result.errors())
            } else {
                val submittedDocument = result.resultingDocument().orElseThrow()
                formFields.forEach { it.postProcess(submittedDocument) }
                publishExternalDataSubmittedEvent(externalFormData, documentDefinitionName, submittedDocument)
                valueResolverService.handleValues(submittedDocument.id.id, remainingValueResolverValues)
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
        documentDefinitionName: String,
        submittedDocument: Document
    ) {
        if (externalFormData.isNotEmpty()) {
            applicationEventPublisher.publishEvent(
                ExternalDataSubmittedEvent(
                    externalFormData,
                    documentDefinitionName,
                    submittedDocument.id().id
                )
            )
        }
    }

    private data class CategorizedSubmitValues(
        val documentValues: ObjectNode,
        val processVariables: Map<String, Any>,
        val otherValues: Map<String, Any>
    )

    companion object {
        val logger = KotlinLogging.logger {}
        const val OTHER = "other"
    }
}
