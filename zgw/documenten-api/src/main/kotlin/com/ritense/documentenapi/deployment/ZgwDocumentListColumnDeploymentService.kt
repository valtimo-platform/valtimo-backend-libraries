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
import com.ritense.documentenapi.deployment.ZgwDocumentListColumnChangeset
import com.ritense.documentenapi.deployment.ZgwDocumentListColumnCollection
import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnId
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

open class ZgwDocumentListColumnDeploymentService(
    private val objectMapper: ObjectMapper,
    private val documentenApiColumnRepository: DocumentenApiColumnRepository,
    private val documentenApiService: DocumentenApiService,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
) : ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.zgw-document-list-column.json"

    override fun before() {
        if (clearTables) {
            documentenApiColumnRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<ZgwDocumentListColumnChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.caseDefinitions,
                key = KEY,
                deploy = { deploy(changeset.caseDefinitions) }
            )
        )
    }

    private fun deploy(caseDefinitions: List<ZgwDocumentListColumnCollection>) {
        runWithoutAuthorization {
            caseDefinitions.forEach {
                documentenApiColumnRepository.deleteAll(documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder(it.key))
                it.columns.forEach { column ->
                    documentenApiService.updateColumn(DocumentenApiColumn(DocumentenApiColumnId(it.key, column)))
                }
            }
        }
    }

    companion object {
        private const val KEY = "case-documenten-api-column"
    }
}