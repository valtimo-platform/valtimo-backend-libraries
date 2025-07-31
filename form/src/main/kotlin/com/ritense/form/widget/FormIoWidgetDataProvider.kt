/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.form.widget

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.document.service.DocumentService
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.service.PrefillFormService
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.DOCUMENT_ID
import com.ritense.widget.WidgetDataProvider
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

class FormIoWidgetDataProvider(
    private val formDefinitionService: FormDefinitionService,
    private val formService: PrefillFormService,
    private val documentService: DocumentService,
) : WidgetDataProvider<FormIoWidget> {

    override fun supportedWidgetType() = FormIoWidget::class.java

    override fun getData(widget: FormIoWidget, properties: Map<String, Any>): JsonNode? {
        val documentId = properties[DOCUMENT_ID]?.toString()
        require(documentId != null) { "Missing documentId" }

        val caseDefinitionId = documentService[documentId].definitionId().caseDefinitionId()

        val formDefinition = formDefinitionService.getFormDefinitionByName(
            widget.properties.formDefinitionName,
            caseDefinitionId
        ).getOrNull()

        return formDefinition?.let {
            formService.getPrefilledFormDefinition(formDefinition.id, UUID.fromString(documentId)).asJson()
        }
    }

}