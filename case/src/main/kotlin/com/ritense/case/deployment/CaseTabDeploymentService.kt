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

package com.ritense.case.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.domain.CaseTabType
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.repository.CaseTabSpecificationHelper.Companion.byCaseDefinitionName
import com.ritense.case.service.CaseTabService
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import com.ritense.logging.withLoggingContext
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@SkipComponentScan
class CaseTabDeploymentService(
    private val objectMapper: ObjectMapper,
    private val caseTabRepository: CaseTabRepository,
    private val changelogService: ChangelogService,
    private val caseTabService: CaseTabService,
    private val clearTables: Boolean
) : ChangesetDeployer {
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

    @Transactional
    @EventListener(DocumentDefinitionDeployedEvent::class)
    fun createCaseTabs(event: DocumentDefinitionDeployedEvent) {
        if (event.documentDefinition().id().version() == 1L) {
            deploy(listOf(CaseDefinitionsTabCollection(event.documentDefinition().id().name(), STANDARD_CASE_TABS)))
        }
    }

    private fun deploy(caseDefinitions: List<CaseDefinitionsTabCollection>) {
        runWithoutAuthorization {
            caseDefinitions.forEach { caseDefinition ->
                withLoggingContext("jsonSchemaDocumentName" to caseDefinition.key) {
                    caseTabRepository.deleteAll(caseTabRepository.findAll(byCaseDefinitionName(caseDefinition.key)))
                    caseDefinition.tabs.map { caseTabDto ->
                        caseTabService.createCaseTab(
                            caseDefinition.key,
                            com.ritense.case.web.rest.dto.CaseTabDto(
                                key = caseTabDto.key,
                                name = caseTabDto.name,
                                type = caseTabDto.type,
                                contentKey = caseTabDto.contentKey
                            )
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val KEY = "case-tab"

        private val STANDARD_CASE_TABS = listOf(
            CaseTabDto("summary", null, CaseTabType.STANDARD, "summary"),
            CaseTabDto("progress", null, CaseTabType.STANDARD, "progress"),
            CaseTabDto("audit", null, CaseTabType.STANDARD, "audit"),
            CaseTabDto("documents", null, CaseTabType.STANDARD, "documents"),
            CaseTabDto("notes", null, CaseTabType.STANDARD, "notes")
        )
    }
}