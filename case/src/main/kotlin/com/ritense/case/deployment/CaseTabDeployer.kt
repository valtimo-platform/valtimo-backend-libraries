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

package com.ritense.case.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.case.domain.CaseTab
import com.ritense.case.domain.CaseTabId
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.byCaseDefinitionName
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

class CaseTabDeployer(
    private val objectMapper: ObjectMapper,
    private val caseTabRepository: CaseTabRepository,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
): ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.case-tabs.json"

    override fun before() {
        if (clearTables) {
            caseTabRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<CaseTabChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.caseDefinitions,
                key = KEY,
                deploy = { deploy(changeset.caseDefinitions) }
            )
        )
    }

    private fun deploy(caseDefinitions: List<CaseDefinitionsTabCollection>) {
        caseDefinitions.forEach {
            caseTabRepository.deleteAll(caseTabRepository.findAll(byCaseDefinitionName(it.key)))
            val tabs = it.tabs.mapIndexed { index, caseTabDto ->
                CaseTab(
                    CaseTabId(it.key, caseTabDto.key),
                    caseTabDto.name,
                    index,
                    caseTabDto.type,
                    caseTabDto.content
                )
            }
            caseTabRepository.saveAll(tabs)
        }
    }

    companion object {
        private const val KEY = "case-tab"
    }
}