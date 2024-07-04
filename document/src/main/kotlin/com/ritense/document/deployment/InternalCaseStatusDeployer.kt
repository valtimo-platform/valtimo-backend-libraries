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
import com.ritense.document.repository.InternalCaseStatusRepository
import com.ritense.document.service.InternalCaseStatusService
import com.ritense.document.web.rest.dto.InternalCaseStatusCreateRequestDto
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

class InternalCaseStatusDeployer(
    private val internalCaseStatusRepository: InternalCaseStatusRepository,
    private val internalCaseStatusService: InternalCaseStatusService,
    private val objectMapper: ObjectMapper,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
) : ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.internal-case-status.json"

    override fun before() {
        if (clearTables) {
            internalCaseStatusRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<InternalCaseStatusChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.internalCaseStatuses,
                key = KEY,
                deploy = { deploy(changeset.internalCaseStatuses) }
            )
        )
    }

    private fun deploy(internalCaseStatuses: List<InternalCaseStatusDto>) {
        AuthorizationContext.runWithoutAuthorization {
            internalCaseStatuses.forEach {
                internalCaseStatusService.create(
                    it.caseDefinitionName,
                    InternalCaseStatusCreateRequestDto(
                        it.key,
                        it.title,
                        it.visibleInCaseListByDefault,
                        it.color
                    )
                )
            }
        }
    }

    companion object {
        private const val KEY = "internal-case-status"
    }
}