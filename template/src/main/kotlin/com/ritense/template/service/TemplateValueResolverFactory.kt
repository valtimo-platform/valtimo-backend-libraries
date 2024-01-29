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

package com.ritense.template.service

import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valueresolver.ValueResolverFactory
import java.util.function.Function
import org.camunda.bpm.engine.delegate.VariableScope

class TemplateValueResolverFactory(
    private val templateService: TemplateService,
    private val documentService: DocumentService,
    private val processDocumentService: ProcessDocumentService,
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return "template"
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        return Function { templateKey ->
            val document = processDocumentService.getDocument(CamundaProcessInstanceId(processInstanceId), variableScope)
            templateService.generate(templateKey, document, variableScope.variables)
        }
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        return Function { templateKey ->
            val document = documentService.get(documentId)
            templateService.generate(templateKey, document)
        }
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any?>
    ) {
        TODO()
    }
}
