/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.case.service

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

open class DocumentDefinitionCreatedListener(
    private val caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository
) {

    @Transactional
    @EventListener(DocumentDefinitionDeployedEvent::class)
    open fun conditionalCreateCase(event: DocumentDefinitionDeployedEvent) {
        val documentDefinitionName = event.documentDefinition().id().name()
        val caseDefinitionSettings = caseDefinitionSettingsRepository.findByIdOrNull(documentDefinitionName)
        if (caseDefinitionSettings == null) {
            caseDefinitionSettingsRepository.save(CaseDefinitionSettings(documentDefinitionName))
        }
    }
}