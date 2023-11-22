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
import com.ritense.case.deployment.CaseDefinitionsTabCollection
import com.ritense.case.deployment.CaseTabChangeset
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.valtimo.contract.domain.ExportFile
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
open class CaseTabExportService(
    private val objectMapper: ObjectMapper,
    private val caseTabService: CaseTabService
) {
    open fun export(caseDefinitionId: JsonSchemaDocumentDefinitionId): Set<ExportFile> {
        val caseName = caseDefinitionId.name()
        val caseTabs = caseTabService.getCaseTabs(caseName)

        val caseTabChangeset = CaseTabChangeset(
            "$caseName.case-tabs",
            listOf(
                CaseDefinitionsTabCollection(
                    caseName,
                    caseTabs.map(CaseTabDto::of)
                )
            )
        )
        val caseTabExport = ExportFile(
            "config/${caseName}.case-tabs.json",
            objectMapper.writeValueAsBytes(caseTabChangeset)
        )

        return setOf(caseTabExport)
    }

    companion object {
        internal const val PATH = "config/%s.case-tabs.json"
    }
}