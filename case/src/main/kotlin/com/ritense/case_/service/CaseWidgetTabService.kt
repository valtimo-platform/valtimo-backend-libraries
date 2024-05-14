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

package com.ritense.case_.service

import com.ritense.case.domain.CaseTabId
import com.ritense.case.domain.CaseTabType
import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.case_.rest.dto.CaseWidgetTabDto
import com.ritense.case_.service.event.CaseTabCreatedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = false)
class CaseWidgetTabService(
    private val caseWidgetTabRepository: CaseWidgetTabRepository
) {

    @EventListener(CaseTabCreatedEvent::class)
    fun handleCaseTabCreatedEvent(event: CaseTabCreatedEvent) {
        if (event.tab.type == CaseTabType.WIDGETS) {
            caseWidgetTabRepository.save(CaseWidgetTab(event.tab.id))
        }
    }

    @Transactional(readOnly = true)
    fun getWidgetTab(caseDefinitionName: String, key: String): CaseWidgetTabDto? {
        return caseWidgetTabRepository.findByIdOrNull(CaseTabId(caseDefinitionName, key))
            ?.let { CaseWidgetTabDto.of(it) }
    }
}