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

package com.ritense.document.exporter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.domain.search.SearchConfigurationDto
import com.ritense.document.service.SearchFieldService
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream

@Transactional(readOnly = true)
class SearchFieldExporter(
    private val objectMapper: ObjectMapper,
    private val searchFieldService: SearchFieldService,
) : Exporter<DocumentDefinitionExportRequest> {

    override fun supports() = DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): ExportResult {
        val searchFields = searchFieldService.getSearchFields(request.name)

        if (searchFields.isEmpty()) {
            return ExportResult()
        }

        val exportFile = ByteArrayOutputStream().use {
            objectMapper.writer(ExportPrettyPrinter()).writeValue(it, SearchConfigurationDto(searchFields))

            ExportFile(
                PATH.format(request.name),
                it.toByteArray()
            )
        }

        return ExportResult(exportFile)
    }

    companion object {
        private const val PATH = "config/search/%s.json"
    }
}