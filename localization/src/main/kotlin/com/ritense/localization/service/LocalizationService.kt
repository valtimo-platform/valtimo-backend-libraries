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

package com.ritense.localization.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.localization.domain.Localization
import com.ritense.localization.repository.LocalizationRepository
import com.ritense.localization.web.rest.dto.LocalizationUpdateRequestDto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Transactional
@Service
@SkipComponentScan
class LocalizationService(
    private val localizationRepository: LocalizationRepository,
    private val objectMapper: ObjectMapper
) {
    @Transactional(readOnly = true)
    fun getLocalizations(): List<Localization> {
        return localizationRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getLocalization(languageKey: String): ObjectNode {
        val localization = localizationRepository.findById(languageKey)

        if (localization.isEmpty) {
            return objectMapper.createObjectNode()
        }

        return localization.get().content
    }

    fun updateLocalization(languageKey: String, localizationContent: ObjectNode): ObjectNode {
        val localization = localizationRepository.findById(languageKey)
            .getOrElse { Localization(languageKey = languageKey, content = localizationContent) }
            .copy(
                languageKey = languageKey,
                content = localizationContent
            )

        return localizationRepository.save(localization).content
    }

    fun updateLocalizations(localizations: List<LocalizationUpdateRequestDto>): List<Localization> {
        val mappedLocalizations = localizations.map {
            Localization(it.languageKey, it.content)
        }
        return localizationRepository.saveAll(mappedLocalizations)
    }
}