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
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.documentenapi.repository.ZgwDocumentTrefwoordRepository
import com.ritense.documentenapi.service.ZgwDocumentTrefwoordService
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@SkipComponentScan
class ZgwDocumentTrefwoordDeploymentService(
    private val objectMapper: ObjectMapper,
    private val zgwDocumentTrefwoordRepository: ZgwDocumentTrefwoordRepository,
    private val zgwDocumentTrefwoordService: ZgwDocumentTrefwoordService,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
) : ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.zgw-document-trefwoorden.json"

    override fun before() {
        if (clearTables) {
            zgwDocumentTrefwoordRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<ZgwDocumentTrefwoordChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.caseDefinitions,
                key = KEY,
                deploy = { deploy(changeset.caseDefinitions) }
            )
        )
    }

    private fun deploy(caseDefinitions: List<CaseDefinitionTrefwoordCollection>) {
        runWithoutAuthorization {
            caseDefinitions.forEach {
                zgwDocumentTrefwoordRepository.deleteAll(zgwDocumentTrefwoordRepository.findAllByCaseDefinitionName(it.key, Pageable.unpaged()))
                it.trefwoorden.forEach { trefwoord ->
                    zgwDocumentTrefwoordService.createTrefwoord(it.key, trefwoord)
                }
            }
        }
    }

    companion object {
        private const val KEY = "case-trefwoord"
    }
}