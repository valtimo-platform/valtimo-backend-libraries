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

package com.ritense.dashboard.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.localization.deployment.LocalizationChangeset
import com.ritense.localization.deployment.LocalizationDto
import com.ritense.localization.domain.Localization
import com.ritense.localization.repository.LocalizationRepository
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService

class LocalizationDeployer(
    private val objectMapper: ObjectMapper,
    private val localizationRepository: LocalizationRepository,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
): ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.localization.json"

    override fun before() {
        if (clearTables) {
            localizationRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<LocalizationChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.localizations,
                key = KEY,
                deploy = { deploy(changeset.localizations) }
            )
        )
    }

    fun deploy(localizations: List<LocalizationDto>) {
        val localizationsToSave = localizations.map {
            createLocalization(it)
        }

        localizationRepository.saveAll(localizationsToSave)
    }

    fun createLocalization(localizationDto: LocalizationDto): Localization {
        val localization = Localization(
            languageKey = localizationDto.languageKey,
            content = localizationDto.content,
        )

        return localization
    }

    companion object {
        private const val KEY = "localization"
    }
}