/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.resolver

import com.ritense.document.domain.Document
import com.ritense.valueresolver.ValueResolverFactory
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valueresolver.exception.ValueResolverValidationException
import org.camunda.bpm.engine.delegate.VariableScope
import java.lang.IllegalArgumentException
import java.util.function.Function

class DocumentTableValueResolver(
    private val processDocumentService: ProcessDocumentService,
    private val documentService: DocumentService,
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return "case"
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        val document = processDocumentService.getDocument(CamundaProcessInstanceId(processInstanceId), variableScope)
        return createResolver(document)
    }

    override fun createValidator(documentDefinitionName: String): Function<String, Unit> {
        return Function { requestedValue ->
            if (!TABLE_COLUMN_LIST.contains(requestedValue)) {
                throw ValueResolverValidationException("Unknown document column with name: $requestedValue")
            }
        }
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        return createResolver(documentService.get(documentId))
    }

    override fun handleValues(processInstanceId: String, variableScope: VariableScope?, values: Map<String, Any>) {
        val firstValue = values.iterator().next()
        throw NotImplementedError("Unable to handle value: {${firstValue.key} to ${firstValue.value}}")
    }

    private fun createResolver(document: Document): Function<String, Any?> {
        return Function { requestedValue ->
            when (requestedValue) {
                "id" -> document.id().id
                "createdOn" -> document.createdOn()
                "createdBy" -> document.createdBy()
                "modifiedOn" -> document.modifiedOn().orElse(null)
                "definitionId" -> document.definitionId()
                "definitionId.name" -> document.definitionId().name()
                "definitionId.version" -> document.definitionId().version()
                "assigneeId" -> document.assigneeId()
                "assigneeFullName" -> document.assigneeFullName()
                "version" -> document.version()
                "sequence" -> document.sequence()
                else -> throw IllegalArgumentException("Unknown document column with name: $requestedValue")
            }
        }
    }

    companion object {
        val TABLE_COLUMN_LIST = listOf(
            "id",
            "createdOn",
            "createdBy",
            "modifiedOn",
            "definitionId",
            "definitionId.name",
            "definitionId.version",
            "assigneeId",
            "assigneeFullName",
            "version",
            "sequence"
        )
    }
}
