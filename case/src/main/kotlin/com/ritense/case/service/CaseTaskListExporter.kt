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

package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case.deployment.CaseDefinitionsTaskListCollection
import com.ritense.case.deployment.TaskListChangeset
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional(readOnly = true)
class CaseTaskListExporter(
    private val objectMapper: ObjectMapper,
    private val taskColumnService: TaskColumnService
) : Exporter<DocumentDefinitionExportRequest> {

    override fun supports() = DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): ExportResult {
        val caseName = request.name
        val caseTaskList = taskColumnService.getListColumns(caseName)
        caseTaskList.forEach {
            it.order = null
        }

        if (caseTaskList.isEmpty()) {
            return ExportResult()
        }

        val caseTaskListChangeset = TaskListChangeset(
            "$caseName.case-task-list.${Instant.now().toEpochMilli()}",
            listOf(
                CaseDefinitionsTaskListCollection(
                    caseName,
                    caseTaskList
                )
            )
        )
        val caseTaskListExport = ExportFile(
            PATH.format(caseName),
            objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(caseTaskListChangeset)
        )

        return ExportResult(
            caseTaskListExport
        )
    }

    companion object {
        private const val PATH = "config/case-task-list/%s.case-task-list.json"
    }
}