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

package com.ritense.smartdocuments.service

import com.fasterxml.jackson.core.JsonPointer
import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.tenancy.TenantResolver
import com.ritense.valtimo.contract.json.Mapper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties

class CamundaSmartDocumentGenerator(
    private val smartDocumentGenerator: SmartDocumentGenerator,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val documentService: DocumentService,
    private val tenantResolver: TenantResolver
) {

    fun generate(execution: DelegateExecution, templateGroup: String, templateId: String, format: DocumentFormatOption) {
        val document = getDocument(execution)
        val templateData = getTemplateData(execution, document)
        smartDocumentGenerator.generateAndStoreDocument(document.id(), templateGroup, templateId, templateData, format)
    }

    private fun getDocument(delegateExecution: DelegateExecution): Document {
        val processInstanceId = CamundaProcessInstanceId(delegateExecution.processInstanceId)
        val processDocumentInstance = processDocumentAssociationService.findProcessDocumentInstance(processInstanceId)
        return if (processDocumentInstance.isPresent) {
            val jsonSchemaDocumentId = processDocumentInstance.get().processDocumentInstanceId().documentId()
            documentService.findBy(jsonSchemaDocumentId, tenantResolver.getTenantId()).orElseThrow()
        } else {
            // In case a process has no token wait state ProcessDocumentInstance is not yet created,
            // therefore out business-key is our last chance which is populated with the documentId also.
            documentService.get(delegateExecution.businessKey, tenantResolver.getTenantId())
        }
    }

    private fun getTemplateData(execution: DelegateExecution, document: Document): Map<String, Any> {
        return execution
            .bpmnModelElementInstance
            .extensionElements
            .elementsQuery
            .filterByType(CamundaProperties::class.java)
            .singleResult()
            .camundaProperties
            .filter { it.camundaName != null && it.camundaValue != null }
            .associate { it.camundaName!! to getPlaceholderValue(it.camundaValue, execution, document) }
    }

    private fun getPlaceholderValue(value: String, execution: DelegateExecution, document: Document): Any {
        return if (value.startsWith("pv:")) {
            getPlaceholderValueFromProcessVariable(value.substring("pv:".length), execution)
        } else if (value.startsWith("doc:")) {
            getPlaceholderValueFromDocument(value.substring("doc:".length), document)
        } else {
            value
        }
    }

    private fun getPlaceholderValueFromProcessVariable(value: String, execution: DelegateExecution): Any {
        return execution.variables[value].toString()
    }

    private fun getPlaceholderValueFromDocument(path: String, document: Document): Any {
        val node = document.content().getValueBy(JsonPointer.valueOf(path)).orElse(null)
        return if (node == null || node.isMissingNode || node.isNull) {
            ""
        } else if (node.isValueNode || node.isArray || node.isObject) {
            Mapper.INSTANCE.get().treeToValue(node, Object::class.java)
        } else {
            node.asText()
        }
    }

}
