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
import com.ritense.case.deployment.CaseTabDto
import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabType
import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportPrettyPrinter
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import com.ritense.exporter.request.ExportRequest
import com.ritense.exporter.request.FormDefinitionExportRequest
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class CaseTabExporter(
    private val objectMapper: ObjectMapper,
    private val caseTabService: CaseTabService
) : Exporter<DocumentDefinitionExportRequest> {

    override fun supports() = DocumentDefinitionExportRequest::class.java

    override fun export(request: DocumentDefinitionExportRequest): ExportResult {
        val caseTabs = caseTabService.getCaseTabs(request.caseDefinitionId)

        if (caseTabs.isEmpty()) {
            return ExportResult()
        }

        val caseDefinitionKey = request.caseDefinitionId.key
        val formattedCaseDefinitionVersion = request.caseDefinitionId.versionTag.let {
            "${it.major}-${it.minor}-${it.patch}"
        }

        val caseTabExport = ExportFile(
            PATH.format(caseDefinitionKey, formattedCaseDefinitionVersion, caseDefinitionKey),
            objectMapper.writer(ExportPrettyPrinter()).writeValueAsBytes(caseTabs.map(CaseTabDto::of))
        )

        return ExportResult(
            caseTabExport,
            createFormDefininitionExportRequests(caseTabs, request.caseDefinitionId)
        )
    }

    private fun createFormDefininitionExportRequests(caseTabs: List<CaseTab>, caseDefinitionId: CaseDefinitionId): Set<ExportRequest> {
        return caseTabs.filter {
            it.type == CaseTabType.FORMIO
        }.distinctBy {
            it.contentKey
        }.map {
            FormDefinitionExportRequest(it.contentKey, caseDefinitionId)
        }.toSet()
    }

    companion object {
        private const val PATH = "config/case/%s/%s/case/tab/%s.case-tabs.json"
    }
}