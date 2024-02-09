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

package com.ritense.form.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.FormDefinitionExportRequest
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class FormDefinitionExporter(
    private val objectMapper: ObjectMapper,
    private val formDefinitionService: FormDefinitionService
) : Exporter<FormDefinitionExportRequest> {
    override fun supports() = FormDefinitionExportRequest::class.java

    override fun export(request: FormDefinitionExportRequest): ExportResult {
        val formDefinition = formDefinitionService.getFormDefinitionByName(request.formDefinitionName).orElseThrow()

        return ExportResult(
            ExportFile(
                PATH.format(formDefinition.name),
                objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(formDefinition.formDefinition)
            )
        )
    }

    companion object {
        private const val PATH = "config/form/%s.json"
    }
}