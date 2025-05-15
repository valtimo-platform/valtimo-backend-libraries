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

import CaseDefinitionDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.CaseDefinitionExportRequest
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class CaseDefinitionExporter(
    private val objectMapper: ObjectMapper,
    private val caseDefinitionService: CaseDefinitionService,
    private val documentDefinitionService: DocumentDefinitionService,
) : Exporter<CaseDefinitionExportRequest> {

    override fun supports() = CaseDefinitionExportRequest::class.java

    override fun export(request: CaseDefinitionExportRequest): ExportResult {
        val caseDefinitionKey = request.caseDefinitionId.key
        val caseDefinition = caseDefinitionService.getCaseDefinition(request.caseDefinitionId)
        val formattedCaseDefinitionVersion = caseDefinition.id.versionTag.let {
            "${it.major}-${it.minor}-${it.patch}"
        }

        val caseDefinitionExport = ExportFile(
            PATH.format(caseDefinitionKey, formattedCaseDefinitionVersion, caseDefinitionKey),
            objectMapper
                .writer(ExportPrettyPrinter())
                .writeValueAsBytes(
                    CaseDefinitionDto(
                        caseDefinition.id.key,
                        caseDefinition.id.versionTag.version,
                        caseDefinition.name,
                        caseDefinition.canHaveAssignee,
                        caseDefinition.autoAssignTasks
                    )
                )
        )

        return ExportResult(caseDefinitionExport, createDocumentDefinitionExportRequest(caseDefinition.id))
    }

    private fun createDocumentDefinitionExportRequest(caseDefinitionId: CaseDefinitionId): Set<DocumentDefinitionExportRequest>  {
        val documentDefinition = documentDefinitionService.findByCaseDefinitionId(caseDefinitionId)
        return if (documentDefinition.isPresent) {
            setOf(DocumentDefinitionExportRequest(documentDefinition.get().id().name(), caseDefinitionId))
        } else {
            emptySet()
        }
    }

    companion object {
        private const val PATH = "config/case/%s/%s/case/definition/%s.case-definition.json"
    }
}