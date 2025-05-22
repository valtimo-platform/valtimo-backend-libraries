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

package com.ritense.document.exporter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.deployment.InternalCaseStatusChangeset
import com.ritense.document.deployment.InternalCaseStatusDto
import com.ritense.document.service.InternalCaseStatusService
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional(readOnly = true)
class InternalCaseStatusExporter(
    private val objectMapper: ObjectMapper,
    private val internalCaseStatusService: InternalCaseStatusService
) : Exporter<DocumentDefinitionExportRequest> {

    override fun supports() = DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): ExportResult {
        val statuses = internalCaseStatusService.getInternalCaseStatuses(request.name)

        if (statuses.isEmpty()) {
            return ExportResult()
        }

        val caseTabChangeset = InternalCaseStatusChangeset(
            "${request.name}.internal-case-status.${Instant.now().toEpochMilli()}",
            statuses.map(InternalCaseStatusDto::of)
        )
        val internalCaseStatusExport = ExportFile(
            PATH.format(request.name),
            objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(caseTabChangeset)
        )

        return ExportResult(internalCaseStatusExport)
    }

    companion object {
        private const val PATH = "config/internal-case-status/%s.internal-case-status.json"
    }
}