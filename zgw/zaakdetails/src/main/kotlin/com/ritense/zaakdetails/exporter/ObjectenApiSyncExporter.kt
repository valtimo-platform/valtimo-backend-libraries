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

package com.ritense.zaakdetails.exporter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncManagementService
import com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncRequest

class ObjectenApiSyncExporter(
    private val objectMapper: ObjectMapper,
    private val documentObjectenApiSyncManagementService: DocumentObjectenApiSyncManagementService,
) : Exporter<DocumentDefinitionExportRequest> {
    override fun supports() = DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): ExportResult {
        val caseName = request.name
        val objectenApiSync = documentObjectenApiSyncManagementService
            .getSyncConfiguration(request.caseDefinitionId)

        if (objectenApiSync == null) {
            return ExportResult()
        }

        val formattedCaseDefinitionVersion = request.caseDefinitionId.versionTag.let {
            "${it.major}-${it.minor}-${it.patch}"
        }


        val zaakTypeLinkExport = ExportFile(
            PATH.format(request.caseDefinitionId.key, formattedCaseDefinitionVersion, caseName),
            objectMapper.writer(ExportPrettyPrinter())
                .writeValueAsBytes(DocumentObjectenApiSyncRequest.of(objectenApiSync))
        )

        return ExportResult(
            zaakTypeLinkExport
        )
    }

    companion object {
        private const val PATH = "config/case/%s/%s/zgw/objecten-api-sync/%s.objecten-api-sync.json"
    }
}