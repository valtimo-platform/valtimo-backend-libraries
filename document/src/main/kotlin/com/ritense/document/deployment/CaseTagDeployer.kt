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

package com.ritense.document.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.repository.CaseTagRepository
import com.ritense.document.service.CaseTagService
import com.ritense.document.web.rest.dto.CaseTagCreateRequestDto
import com.ritense.document.web.rest.dto.CaseTagUpdateRequestDto
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

class CaseTagDeployer(
    private val caseTagRepository: CaseTagRepository,
    private val caseTagService: CaseTagService,
    private val objectMapper: ObjectMapper,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
) : ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.case-tag.json"

    // TODO discuss with product team how to handle deletions
    override fun before() {
        if (clearTables) {
            caseTagRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<CaseTagChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.caseTags,
                key = KEY,
                deploy = { deploy(changeset.caseTags) }
            )
        )
    }

    private fun deploy(caseTags: List<CaseTagDto>) {
        AuthorizationContext.runWithoutAuthorization {
            caseTags.forEach {
                if ( ! caseTagService.exists(it.caseDefinitionName, it.key) ) {
                    caseTagService.create(
                        it.caseDefinitionName,
                        CaseTagCreateRequestDto(
                            it.key,
                            it.title,
                            it.color
                        )
                    )
                } else {
                    caseTagService.update(
                        it.caseDefinitionName,
                        it.key,
                        CaseTagUpdateRequestDto(
                            it.key,
                            it.title,
                            it.color
                        )
                    )
                }
            }
        }
    }

    companion object {
        private const val KEY = "case-tag"
    }
}