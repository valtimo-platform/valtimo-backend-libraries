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

package com.ritense.localization.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.localization.domain.Localization
import com.ritense.localization.exception.LocalizationNotFoundException
import com.ritense.localization.repository.LocalizationRepository
import mu.KLogger
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Transactional
class LocalizationService(
    private val localizationRepository: LocalizationRepository
) {
    @Transactional(readOnly = true)
    fun getLocalizations(): List<Localization> {
        return localizationRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getLocalization(languageKey: String): ObjectNode {
        val localization = localizationRepository.findById(languageKey)

        if (localization.isEmpty) {
            throw LocalizationNotFoundException(languageKey)
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

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}