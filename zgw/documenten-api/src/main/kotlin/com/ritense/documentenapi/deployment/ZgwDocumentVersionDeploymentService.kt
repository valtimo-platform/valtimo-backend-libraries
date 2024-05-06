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

package com.ritense.documentenapi.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.documentenapi.repository.DocumentenApiVersionRepository
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

open class ZgwDocumentVersionDeploymentService(
    private val objectMapper: ObjectMapper,
    private val documentenApiVersionRepository: DocumentenApiVersionRepository,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean,
) : ChangesetDeployer {

    override fun getPath() = "classpath*:**/*.zgw-document-version.json"

    override fun before() {
        if (clearTables) {
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<ZgwDocumentVersionChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.documentenApiVersions,
                key = KEY,
                deploy = { deploy(changeset.documentenApiVersions) }
            )
        )
    }

    fun deploy(versions: List<DocumentenApiVersionWithFeatures>) {
        documentenApiVersionRepository.saveAll(versions.map { it.toEntity() })
    }

    companion object {
        const val KEY = "case-documenten-api-version"
    }

}