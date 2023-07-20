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

package com.ritense.valtimo.formflow.common

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.formflow.domain.instance.FormFlowInstanceId
import com.ritense.formflow.expression.FormFlowBean
import com.ritense.formflow.service.FormFlowService
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import org.springframework.transaction.annotation.Transactional
import java.util.Objects
import java.util.UUID

@FormFlowBean
open class ValtimoFormFlow(
    private val taskService: CamundaTaskService,
    private val objectMapper: ObjectMapper,
    private val valueResolverService: ValueResolverService,
    private val formFlowService: FormFlowService,
    private val processDocumentService: ProcessDocumentService,
    private val documentService: DocumentService
) {

    /**
     * Completes a Camunda user task
     *
     * @param additionalProperties provided by Form Flow
     */
    @Transactional
    open fun completeTask(additionalProperties: Map<String, Any>) {
        return completeTask(additionalProperties, null)
    }

    /**
     * Completes a Camunda user task and save the submission in the document
     *
     * @param additionalProperties provided by Form Flow
     * @param submissionData the data that was submitted at the end of a Form Flow step
     */
    @Transactional
    open fun completeTask(additionalProperties: Map<String, Any>, submissionData: JsonNode?) {
        return completeTask(additionalProperties, submissionData, mapOf("doc:/submission" to ""))
    }

    /**
     * Completes a Camunda user task and save the submission in a place defined in submissionSavePath
     *
     * @param additionalProperties provided by Form Flow
     * @param submissionData the data that was submitted at the end of a Form Flow step
     * @param submissionSavePath where the submission data should be saved. The key should be the save location, the value it the path in the submissionData.
     */
    @Transactional
    open fun completeTask(
        additionalProperties: Map<String, Any>,
        submissionData: JsonNode?,
        submissionSavePath: Map<String, String>
    ) {
        if (submissionData != null) {
            val processInstanceId = additionalProperties["processInstanceId"] as String
            val submissionValues = submissionSavePath.entries.associate { it.key to getValue(submissionData, it.value) }
            valueResolverService.handleValues(processInstanceId, null, submissionValues)
        }

        taskService.complete(additionalProperties["taskInstanceId"] as String)
    }

    /**
     * Starts a new case by creating a document and starting a process.
     */
    @Transactional
    open fun startCase(
        formFlowInstanceId: FormFlowInstanceId,
        submissionSavePath: Map<String, String>
    ) {
        val formFlowInstance = formFlowService.getInstanceById(formFlowInstanceId)
        val documentDefinitionName = getRequiredAdditionalProperty(
            formFlowInstance.getAdditionalProperties(),
            "documentDefinitionName"
        ).toString()

        val submission = jacksonObjectMapper().readValue<JsonNode>(formFlowInstance.getSubmissionDataContext())

        val submissionValues = submissionSavePath.entries.associate { it.key to getValue(submission, it.value) }
        val submittedByType = valueResolverService.preProcessValuesForNewCase(submissionValues)

        val document = AuthorizationContext.runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    documentDefinitionName,
                    submittedByType["doc"] as JsonNode
                )
            )
        }.also { result ->
            if (result.errors().size > 0) {
                throw RuntimeException(
                    "Could not create document for document definition $documentDefinitionName\n" +
                        "Reason:\n" +
                        result.errors().joinToString(separator = "\n - ")
                )
            }
        }.resultingDocument().orElseThrow()

        val processDefinitionKey = getRequiredAdditionalProperty(formFlowInstance.getAdditionalProperties(), "processDefinitionKey").toString()
        val startProcessForDocumentRequest = StartProcessForDocumentRequest(
            document.id(),
            processDefinitionKey,
            submittedByType["pv"] as Map<String, Any>?
        )
        //TODO: PBAC START/CREATE check
        val startProcessForDocumentResult = AuthorizationContext.runWithoutAuthorization {
            processDocumentService.startProcessForDocument(startProcessForDocumentRequest)
        }
        if (startProcessForDocumentResult.errors().isNotEmpty()) {
            throw RuntimeException(
                "Could not start process with definition $processDefinitionKey for document ${document.id()}\n" +
                    "Reason:\n" +
                    startProcessForDocumentResult.errors().joinToString(separator = "\n - ")
            )
        }
    }

    /**
     * Starts a process for an existing case
     */
    @Transactional
    open fun startSupportingProcess(
        formFlowInstanceId: FormFlowInstanceId,
        submissionSavePath: Map<String, String>
    ) {
        val formFlowInstance = formFlowService.getInstanceById(formFlowInstanceId)
        val documentId = getRequiredAdditionalProperty(
            formFlowInstance.getAdditionalProperties(),
            "documentId"
        ).toString()

        val submission = jacksonObjectMapper().readValue<JsonNode>(formFlowInstance.getSubmissionDataContext())
        val submissionValues = submissionSavePath.entries.associate { it.key to getValue(submission, it.value) }
        valueResolverService.handleValues(UUID.fromString(documentId), submissionValues)
        val submittedByType = valueResolverService.preProcessValuesForNewCase(submissionValues)

        val processDefinitionKey = getRequiredAdditionalProperty(formFlowInstance.getAdditionalProperties(), "processDefinitionKey").toString()
        val startProcessForDocumentRequest = StartProcessForDocumentRequest(
            JsonSchemaDocumentId.existingId(UUID.fromString(documentId)),
            processDefinitionKey,
            submittedByType["pv"] as Map<String, Objects>?
        )
        //TODO: PBAC START/CREATE check
        val startProcessForDocumentResult = AuthorizationContext.runWithoutAuthorization {
            processDocumentService.startProcessForDocument(startProcessForDocumentRequest)
        }
        if (startProcessForDocumentResult.errors().isNotEmpty()) {
            throw RuntimeException(
                "Could not start process with definition $processDefinitionKey for document ${documentId}\n" +
                    "Reason:\n" +
                    startProcessForDocumentResult.errors().joinToString(separator = "\n - ")
            )
        }
    }

    private fun getRequiredAdditionalProperty(additionalProperties: Map<String, Any>, propertyName: String): Any {
        if (!additionalProperties.containsKey(propertyName))
            throw IllegalStateException("Properties for form flow does not contain $propertyName")
        return additionalProperties[propertyName]!!
    }

    private fun getValue(data: JsonNode, path: String): Any {
        val valueNode = data.at(JsonPointer.valueOf(path))
        if (valueNode.isMissingNode) {
            throw RuntimeException("Missing data on path '$path'")
        }
        return objectMapper.treeToValue(valueNode, Object::class.java)
    }
}
