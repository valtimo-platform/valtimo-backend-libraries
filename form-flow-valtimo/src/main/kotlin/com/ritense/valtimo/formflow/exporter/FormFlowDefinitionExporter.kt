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

package com.ritense.valtimo.formflow.exporter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.FormDefinitionExportRequest
import com.ritense.exporter.request.FormFlowDefinitionExportRequest
import com.ritense.formflow.domain.definition.configuration.FormFlowDefinition
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.service.FormFlowService
import com.ritense.logging.withLoggingContext
import com.ritense.valtimo.formflow.handler.FormFlowStepTypeFormHandler
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class FormFlowDefinitionExporter(
    private val objectMapper: ObjectMapper,
    private val formFlowService: FormFlowService
) : Exporter<FormFlowDefinitionExportRequest> {

    override fun supports() = FormFlowDefinitionExportRequest::class.java

    override fun export(request: FormFlowDefinitionExportRequest): ExportResult {
        return withLoggingContext(FormFlowDefinition::class, request.formFlowDefinitionId) {
            val definition = requireNotNull(formFlowService.findDefinition(request.formFlowDefinitionId))

            val relatedRequests = definition.steps.map { step ->
                step.type
            }.filter { type ->
                type.name == FormFlowStepTypeFormHandler.TYPE
            }.map { type ->
                val formDefinitionName = (type.properties as FormStepTypeProperties).definition
                FormDefinitionExportRequest(formDefinitionName)
            }.toSet()

            ExportResult(
                ExportFile(
                    PATH.format(definition.id.key),
                    objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(FormFlowDefinition.fromEntity(definition))
                ),
                relatedRequests
            )
        }
    }

    companion object {
        private const val PATH = "config/form-flow/%s.json"
    }
}