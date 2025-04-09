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

package com.ritense.form.service.impl

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.AuthorizationResourceContext
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.authorization.request.RelatedEntityAuthorizationRequest
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.domain.submission.formfield.FormField
import com.ritense.form.service.FormSubmissionService
import com.ritense.form.service.PrefillFormService
import com.ritense.form.web.rest.dto.FormSubmissionResult
import com.ritense.form.web.rest.dto.FormSubmissionResultFailed
import com.ritense.form.web.rest.dto.FormSubmissionResultSucceeded
import com.ritense.logging.LoggableResource
import com.ritense.logging.withLoggingContext
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
import com.ritense.valtimo.camunda.authorization.CamundaExecutionActionProvider
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider.Companion.COMPLETE
import com.ritense.valtimo.camunda.domain.CamundaExecution
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import com.ritense.valtimo.contract.json.patch.JsonPatch
import com.ritense.valtimo.contract.result.OperationError
import com.ritense.valtimo.contract.result.OperationError.FromException
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.valueresolver.ValueResolverServiceImpl
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import com.ritense.processdocument.resolver.DocumentJsonValueResolverFactory.Companion.PREFIX as DOC_PREFIX
import com.ritense.valueresolver.ProcessVariableValueResolverFactory.Companion.PREFIX as PV_PREFIX

@Service
@SkipComponentScan
class DefaultFormSubmissionService(
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
    private val valueResolverService: ValueResolverService,
    private val objectMapper: ObjectMapper,
) : FormSubmissionService {

    @Transactional
    override fun handleSubmission(
        @LoggableResource(resourceType = ProcessLink::class) processLinkId: UUID,
        formData: JsonNode,
        @LoggableResource("documentDefinitionName") documentDefinitionName: String?,
        @LoggableResource(resourceType = JsonSchemaDocument::class) documentId: String?,
        @LoggableResource(resourceType = CamundaTask::class) taskInstanceId: String?,
    ): FormSubmissionResult {
        return try {
            // TODO: Implement else, done by verifying what the processLink contains
            val document = documentId
                ?.let { runWithoutAuthorization { documentService.get(documentId) } }
            val processLink = processLinkService.getProcessLink(processLinkId, FormProcessLink::class.java)
            requirePermission(taskInstanceId, document, processLink.processDefinitionId)

            val processDefinition = getProcessDefinition(processLink)
            val documentDefinitionNameToUse = document?.definitionId()?.name()
                ?: documentDefinitionName
                ?: getProcessDocumentDefinition(processDefinition, document).processDocumentDefinitionId()
                    .documentDefinitionId().name()
            val processVariables = getProcessVariables(taskInstanceId)
            val formDefinition = formDefinitionService.getFormDefinitionById(processLink.formDefinitionId).orElseThrow()

            val formFields = getFormFields(formDefinition, formData)
            preProcessFormFields(formFields, document)
            val categorizedKeyValues = getCategorizedSubmitValues(formDefinition, formData, document)

            val modifyDocumentWithJsonPatch = getPreJsonPatch(
                formDefinition, categorizedKeyValues.modifyDocumentWithJsonPatchValues, processVariables, document
            )
            val request = getRequest(
                processLink,
                document,
                taskInstanceId,
                documentDefinitionNameToUse,
                processDefinition.key,
                categorizedKeyValues.createDocumentWithContent,
                categorizedKeyValues.withProcessVars,
                modifyDocumentWithJsonPatch
            )

            val externalFormData = getExternalFormData(formDefinition, formData)
            return dispatchRequest(
                request,
                formFields,
                externalFormData,
                documentDefinitionNameToUse,
                categorizedKeyValues.valueResolverValues
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

    private fun requirePermission(taskInstanceId: String?, document: JsonSchemaDocument?, processDefinitionId: String) {
        if (taskInstanceId != null) {
            val task = camundaTaskService.findTaskById(taskInstanceId)
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    CamundaTask::class.java,
                    COMPLETE,
                    task
                )
            )
        } else {
            authorizationService.requirePermission(
                RelatedEntityAuthorizationRequest(
                    CamundaExecution::class.java,
                    CamundaExecutionActionProvider.CREATE,
                    CamundaProcessDefinition::class.java,
                    processDefinitionId
                ).apply {
                    if (document != null) {
                        withContext(
                            AuthorizationResourceContext(
                                JsonSchemaDocument::class.java,
                                document
                            )
                        )
                    }
                }
            )
        }
    }

    /**
     * This method categorizes the submitted values into 4 categories:
     * - createDocumentWithContent: A new document is created with this as its JSON content
     * - withProcessVar: Add this variable to the process
     * - modifyDocumentWithJsonPatchValue: An existing document is modified with this JSON patch
     * - valueResolverValue: The case is updated with this valueResolverValue
     */
    private fun getCategorizedSubmitValues(
        formDefinition: FormIoFormDefinition,
        formData: JsonNode,
        document: Document?
    ): CategorizedSubmitValues {
        val categorizedMap = formDefinition.inputFields
            .filter { FormIoFormDefinition.NOT_IGNORED.test(it) }
            .filter { FormIoFormDefinition.isInputComponent(it) }
            .mapNotNull { field ->
                getTargetKeyValuePair(field, formData)
            }
            .groupBy { it.first.substringBefore("/-/") }
            .flatMap { (_, groups) ->
                groups.mapIndexed { i, (targetKey, value) ->
                    val newTargetKey = if (i == 0) targetKey else targetKey.replace("/-/", "/+/")
                    newTargetKey to value
                }
            }.groupBy { (key, _) ->
                val prefix = key.substringBefore(ValueResolverServiceImpl.DELIMITER, missingDelimiterValue = "")
                if (prefix == DOC_PREFIX && key.contains("{indexOf")) {
                    "modifyDocumentWithJsonPatchValue"
                } else if (prefix == DOC_PREFIX && document == null) {
                    "createDocumentWithContent"
                } else if (prefix == PV_PREFIX) {
                    "withProcessVar"
                } else {
                    "valueResolverValue"
                }
            }.mapValues { it.value.toMap() }

        // Preprocess the document paths & values. The result is an ObjectNode.
        val createDocumentWithContent = categorizedMap["createDocumentWithContent"]
            ?.let { valueResolverService.preProcessValuesForNewCase(it)[DOC_PREFIX] as? ObjectNode }
            ?: objectMapper.createObjectNode()

        // After pre-processing process-variables we have a key-value map where the prefix is stripped from the keys.
        val withProcessVars = categorizedMap["withProcessVar"]
            ?.let { valueResolverService.preProcessValuesForNewCase(it)[PV_PREFIX] as? Map<String, Any> }
            ?: mapOf()

        // Do not process/handle other values yet.
        val valueResolverValues = categorizedMap["valueResolverValue"] ?: mapOf()
        val modifyDocumentWithJsonPatchValues = categorizedMap["modifyDocumentWithJsonPatchValue"] ?: mapOf()

        return CategorizedSubmitValues(
            createDocumentWithContent,
            withProcessVars,
            valueResolverValues,
            modifyDocumentWithJsonPatchValues
        )
    }

    private fun getTargetKeyValuePair(
        field: ObjectNode,
        formData: JsonNode
    ): Pair<String, Any>? {
        return FormIoFormDefinition.resolveTargetKey(field).getOrNull()?.let { targetKey ->
            getFormValue(field, formData)?.let { value -> Pair(targetKey, value) }
        }
    }

    private fun getFormValue(field: ObjectNode, formData: JsonNode): Any? {
        return FormIoFormDefinition.getKey(field).getOrNull()?.let { inputKey ->
            val jsonPointer = JsonPointer.compile("/${inputKey.replace('.', '/')}")
            if (field.at(FormIoFormDefinition.PROPERTIES_CONTAINER_POINTER).isTextual) {
                consumeValue(formData, jsonPointer)
            } else {
                convertNodeValue(formData.at(jsonPointer))
            }
        }
    }

    private fun consumeValue(formData: JsonNode, jsonPointer: JsonPointer): Any? {
        val valueNode = formData.at(jsonPointer)
        if (!valueNode.isMissingNode) {
            val head = jsonPointer.head()
            if (formData.at(head).isObject) {
                (formData.at(head) as ObjectNode).remove(jsonPointer.last().matchingProperty)
            } else if (formData.at(head).isArray) {
                (formData.at(head) as ArrayNode).remove(jsonPointer.last().matchingProperty.toInt())
            }
        }
        return convertNodeValue(valueNode)
    }

    private fun convertNodeValue(node: JsonNode): Any? {
        if (node.isMissingNode) {
            return null
        }

        return objectMapper.treeToValue<Any>(node)
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
            objectMapper.valueToTree(camundaTaskService.getVariables(taskInstanceId))
        } else {
            null
        }
    }

    private fun getFormFields(
        formDefinition: FormIoFormDefinition,
        formData: JsonNode
    ): List<FormField> {
        return formDefinition.inputFields
            .filter { FormIoFormDefinition.NOT_IGNORED.test(it) }
            .mapNotNull { field -> FormField.getFormField(formData, field, applicationEventPublisher) }
    }

    private fun preProcessFormFields(formFields: List<FormField>, document: Document?) {
        formFields.forEach {
            it.preProcess(document)
        }
    }

    private fun getPreJsonPatch(
        formDefinition: FormIoFormDefinition,
        jsonPatchValues: Map<String, Any>,
        processVariables: JsonNode?,
        document: Document?
    ): JsonPatch {
        val jsonPatchNode = objectMapper.valueToTree<JsonNode>(
            jsonPatchValues.entries.associate { e -> e.key.substringAfter(")}/") to e.value }
        )

        //Note: Pre patch can be refactored into a specific field types that apply itself
        val preJsonPatch = prefillFormService.preSubmissionTransform(
            formDefinition,
            jsonPatchNode,
            processVariables ?: objectMapper.createObjectNode(),
            document?.content()?.asJson() ?: objectMapper.createObjectNode()
        )
        logger.debug { "getContent:$jsonPatchValues" }
        return preJsonPatch
    }

    private fun getExternalFormData(
        formDefinition: FormIoFormDefinition,
        formData: JsonNode
    ): Map<String, Map<String, JsonNode>> {
        return formDefinition.buildExternalFormFieldsMapFiltered(
            FormIoFormDefinition.NOT_IGNORED.and { t ->
                FormIoFormDefinition.getTargetKey(t).isEmpty && FormIoFormDefinition.getSourceKey(t).isEmpty
            }
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
        documentContent: JsonNode,
        withProcessVars: Map<String, Any>,
        modifyDocumentWithJsonPatch: JsonPatch
    ): Request {
        return if (processLink.activityType == START_EVENT_START) {
            check(taskInstanceId == null) {
                "Process link configuration error: START_EVENT_START shouldn't be linked to a user-task. For process-definition: '${processLink.processDefinitionId}' with activity-id: '${processLink.activityId}'"
            }
            if (document == null) {
                newDocumentAndStartProcessRequest(
                    documentDefinitionName,
                    processDefinitionKey,
                    documentContent,
                    withProcessVars
                )
            } else {
                modifyDocumentAndStartProcessRequest(
                    document,
                    processDefinitionKey,
                    documentContent,
                    withProcessVars,
                    modifyDocumentWithJsonPatch
                )
            }
        } else if (processLink.activityType == USER_TASK_CREATE) {
            check(document != null && taskInstanceId != null) {
                "Process link configuration error: USER_TASK_CREATE shouldn't be linked to a start-event. For process-definition: '${processLink.processDefinitionId}' with activity-id: '${processLink.activityId}'"
            }
            modifyDocumentAndCompleteTaskRequest(
                document,
                taskInstanceId,
                documentContent,
                withProcessVars,
                modifyDocumentWithJsonPatch
            )
        } else {
            throw UnsupportedOperationException("Cannot handle submission for activity-type '" + processLink.activityType + "'")
        }
    }

    private fun newDocumentAndStartProcessRequest(
        documentDefinitionName: String,
        processDefinitionKey: String,
        documentContent: JsonNode,
        withProcessVars: Map<String, Any>,
    ): NewDocumentAndStartProcessRequest {
        return NewDocumentAndStartProcessRequest(
            processDefinitionKey,
            NewDocumentRequest(
                documentDefinitionName,
                documentContent
            )
        ).withProcessVars(withProcessVars)
    }

    private fun modifyDocumentAndStartProcessRequest(
        document: Document,
        processDefinitionKey: String,
        documentContent: JsonNode,
        withProcessVars: Map<String, Any>,
        withJsonPatch: JsonPatch
    ): ModifyDocumentAndStartProcessRequest {
        return ModifyDocumentAndStartProcessRequest(
            processDefinitionKey,
            ModifyDocumentRequest(
                document.id().toString(),
                documentContent
            ).withJsonPatch(withJsonPatch)
        ).withProcessVars(withProcessVars)
    }

    private fun modifyDocumentAndCompleteTaskRequest(
        document: Document,
        taskInstanceId: String,
        documentContent: JsonNode,
        withProcessVars: Map<String, Any>,
        withJsonPatch: JsonPatch
    ): ModifyDocumentAndCompleteTaskRequest {
        return ModifyDocumentAndCompleteTaskRequest(
            ModifyDocumentRequest(
                document.id().toString(),
                documentContent
            ).withJsonPatch(withJsonPatch),
            taskInstanceId
        ).withProcessVars(withProcessVars)
    }

    private fun dispatchRequest(
        request: Request,
        formFields: List<FormField>,
        externalFormData: Map<String, Map<String, JsonNode>>,
        documentDefinitionName: String,
        remainingValueResolverValues: Map<String, Any>,
    ): FormSubmissionResult {
        return try {
            val result = processDocumentService.dispatch(
                request.withAdditionalModifications { document: JsonSchemaDocument ->
                    withLoggingContext(JsonSchemaDocument::class, document.id()) {
                        formFields.forEach { it.postProcess(document) }
                        publishExternalDataSubmittedEvent(externalFormData, documentDefinitionName, document)
                        valueResolverService.handleValues(document.id.id, remainingValueResolverValues)
                    }
                }
            )
            return if (result.errors().isNotEmpty()) {
                FormSubmissionResultFailed(result.errors())
            } else {
                val submittedDocument = result.resultingDocument().orElseThrow()
                withLoggingContext(JsonSchemaDocument::class, submittedDocument.id()) {
                    FormSubmissionResultSucceeded(submittedDocument.id().toString())
                }
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
        val createDocumentWithContent: ObjectNode,
        val withProcessVars: Map<String, Any>,
        val valueResolverValues: Map<String, Any>,
        val modifyDocumentWithJsonPatchValues: Map<String, Any>
    )

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
