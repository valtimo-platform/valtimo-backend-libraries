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

package com.ritense.case.deployment

import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.importer.ImportContext
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@SkipComponentScan
class CaseTabDeploymentService(
    private val caseTabService: CaseTabService
) {
    @Transactional
    @EventListener(CaseDefinitionCreatedEvent::class)
    fun createCaseTabs(event: CaseDefinitionCreatedEvent) {
        // only create default case tabs if not importing (because we assume the import should contain all tabs the user wants)
        // and only if it's a new case definition (because we assume they would get copied from the previous case definition)
        if (!ImportContext.isImporting() && event.basedOnCaseDefinitionId == null) {
            STANDARD_CASE_TABS.forEach { caseTabDto ->
                caseTabService.createCaseTab(event.caseDefinitionId, caseTabDto)
            }
        }
    }

    private companion object {
        private val STANDARD_CASE_TABS = listOf(
            CaseTabDto("summary", null, CaseTabType.STANDARD, "summary"),
            CaseTabDto("progress", null, CaseTabType.STANDARD, "progress"),
            CaseTabDto("audit", null, CaseTabType.STANDARD, "audit"),
            CaseTabDto("documents", null, CaseTabType.STANDARD, "documents"),
            CaseTabDto("notes", null, CaseTabType.STANDARD, "notes")
        )
    }
}