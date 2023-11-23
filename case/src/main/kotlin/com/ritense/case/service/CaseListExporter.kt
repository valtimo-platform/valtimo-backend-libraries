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

package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.export.ExportFile
import com.ritense.export.Exporter
import com.ritense.export.request.DocumentDefinitionExportRequest
import java.io.ByteArrayOutputStream

class CaseListExporter(
    private val caseDefinitionService: CaseDefinitionService,
    private val mapper: ObjectMapper
) : Exporter<DocumentDefinitionExportRequest> {

    override fun supports(): Class<DocumentDefinitionExportRequest> =
        DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): Set<ExportFile> {
        val listColumns = caseDefinitionService.getListColumns(request.name)

        val exportFile = ByteArrayOutputStream().use {
            mapper.writerWithDefaultPrettyPrinter().writeValue(it, (listColumns))

            ExportFile(
                PATH.format(request.name),
                it.toByteArray()
            )
        }

        return setOf(exportFile)
    }

    companion object {
        private const val PATH = "config/case/list/%s.json"
    }
}