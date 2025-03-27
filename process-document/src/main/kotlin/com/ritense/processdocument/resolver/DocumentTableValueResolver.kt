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

package com.ritense.processdocument.resolver

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valueresolver.ValueResolverFactory
import com.ritense.valueresolver.ValueResolverOption
import com.ritense.valueresolver.exception.ValueResolverValidationException
import org.camunda.bpm.engine.delegate.VariableScope
import java.util.function.Function

/**
 * This resolver can resolve requestedValues against the Document table columns
 *
 * The value of the requestedValue should be in the format case:assigneeFullName
 */
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
        return AuthorizationContext.runWithoutAuthorization { createResolver(documentService.get(documentId)) }
    }

    override fun handleValues(processInstanceId: String, variableScope: VariableScope?, values: Map<String, Any?>) {
        val firstValue = values.iterator().next()
        throw NotImplementedError("Unable to handle value: {${firstValue.key} to ${firstValue.value}}")
    }

    override fun getResolvableKeyOptions(documentDefinitionName: String, caseDefinitionId: CaseDefinitionId): List<ValueResolverOption> {
        return createFieldList(TABLE_COLUMN_LIST)
    }

    override fun getResolvableKeyOptions(documentDefinitionName: String): List<ValueResolverOption> {
        return createFieldList(TABLE_COLUMN_LIST)
    }

    private fun createResolver(document: Document): Function<String, Any?> {
        return Function { requestedValue ->
            when (requestedValue) {
                "assigneeFullName" -> document.assigneeFullName()
                "assigneeId" -> document.assigneeId()
                "createdBy" -> document.createdBy()
                "createdOn" -> document.createdOn()
                "definitionId" -> document.definitionId()
                "definitionId.name" -> document.definitionId().name()
                // TODO: change definitionId.caseDefinitionId
                "definitionId.version" -> document.definitionId().caseDefinitionId()
                "id" -> document.id().id
                "internalStatus" -> document.internalStatus()
                "modifiedOn" -> document.modifiedOn().orElse(null)
                "sequence" -> document.sequence()
                "version" -> document.version()
                else -> throw IllegalArgumentException("Unknown document column with name: $requestedValue")
            }
        }
    }

    companion object {
        val TABLE_COLUMN_LIST = listOf(
            "assigneeFullName",
            "assigneeId",
            "createdBy",
            "createdOn",
            "definitionId.name",
            "definitionId.version",
            "id",
            "internalStatus",
            "modifiedOn",
            "sequence",
            "version",
        )
    }
}
