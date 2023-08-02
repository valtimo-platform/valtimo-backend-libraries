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

package com.ritense.processdocument.service

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.Document
import com.ritense.document.domain.DocumentVersion
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.valtimo.contract.json.Mapper
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.time.LocalDateTime
import java.util.*


class DocumentDelegateService(
    private val processDocumentService: ProcessDocumentService,
    private val documentService: DocumentService,
    private val jsonSchemaDocumentService: JsonSchemaDocumentService
) {

    private val mapper = Mapper.INSTANCE.get()

    fun getDocumentVersion(execution: DelegateExecution): DocumentVersion? {
        logger.debug("Get version of document {}", execution.processBusinessKey)
        return getDocumentById(execution).version()
    }

    fun getDocumentCreatedOn(execution: DelegateExecution): LocalDateTime? {
        logger.debug("Get created on date of document {}", execution.processBusinessKey)
        return getDocumentById(execution).createdOn()
    }

    fun getDocumentCreatedBy(execution: DelegateExecution): String? {
        logger.debug("Get created by of document {}", execution.processBusinessKey)
        return getDocumentById(execution).createdBy()
    }

    fun getDocumentModifiedOn(execution: DelegateExecution): Optional<LocalDateTime>? {
        logger.debug("Get modified on of document {}", execution.processBusinessKey)
        return getDocumentById(execution).modifiedOn()
    }

    fun getDocumentAssigneeId(execution: DelegateExecution): String? {
        logger.debug("Get assigneeId of document {}", execution.processBusinessKey)
        return getDocumentById(execution).assigneeId()
    }

    fun getDocumentAssigneeFullName(execution: DelegateExecution): String? {
        logger.debug("Get assignee full name of document {}", execution.processBusinessKey)
        return getDocumentById(execution).assigneeFullName()
    }

    fun getDocumentById(execution: DelegateExecution): Document {
        val documentId = processDocumentService.getDocumentId(CamundaProcessInstanceId(execution.processInstanceId), execution)
        return jsonSchemaDocumentService.getDocumentBy(documentId)
    }

    open fun findValueByJsonPointer(jsonPointer: String?, execution: DelegateExecution?): Any? {
        return Optional.ofNullable(findValueByJsonPointerOrDefault(jsonPointer, execution!!, null))
            .orElseThrow()
    }

    fun findValueByJsonPointerOrDefault(jsonPointer: String?, execution: DelegateExecution, defaultValue: Any?): Any? {
        val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(execution.processBusinessKey))
        logger.debug("Retrieving value for key {} from documentId {}", jsonPointer, execution.processBusinessKey)
        return AuthorizationContext.runWithoutAuthorization { documentService.findBy(jsonSchemaDocumentId)
        }
            .flatMap { jsonSchemaDocument -> jsonSchemaDocument.content().getValueBy(JsonPointer.valueOf(jsonPointer))
            }
            .map(::transform)
            .orElse(defaultValue)
    }

    private fun transform(jsonNode: JsonNode): Any? {
        if (jsonNode.isNumber) {
            // Removing this would result in a breaking change, as 3.0 will become an int when using treeToValue
            return jsonNode.asDouble()
        } else if (jsonNode.isValueNode || jsonNode.isContainerNode) {
            try {
                return mapper.treeToValue(jsonNode, Any::class.java)
            } catch (e: JsonProcessingException) {
                logger.error("Could not transform JsonNode of type \"" + jsonNode.nodeType + "\"", e)
            }
        } else {
            logger.debug(
                "JsonNode of type \"" + jsonNode.nodeType + "\" cannot be transformed to a value. Returning null."
            )
        }
        return null
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }


}