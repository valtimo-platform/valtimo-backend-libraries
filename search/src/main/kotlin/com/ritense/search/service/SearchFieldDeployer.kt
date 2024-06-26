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

package com.ritense.search.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.search.domain.SearchFieldChangeset
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

abstract class SearchFieldDeployer(
    private val objectMapper: ObjectMapper,
    private val changelogService: ChangelogService,
    private val repository: SearchFieldV2Repository,
    private val clearTables: Boolean,
): ChangesetDeployer {

    override fun before() {
        if (clearTables) {
            repository.deleteAllByOwnerType(ownerTypeKey())
            changelogService.deleteChangesetsByKey(changeSetKey())
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<SearchFieldChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.caseDefinitions,
                key = ownerTypeKey(),
                deploy = { deploy(changeset.caseDefinitions) }
            )
        )
    }

    fun deploy(searchFields: List<SearchFieldV2>) {
        repository.saveAll(searchFields)
    }

    abstract fun ownerTypeKey(): String
    abstract fun changeSetKey(): String
}