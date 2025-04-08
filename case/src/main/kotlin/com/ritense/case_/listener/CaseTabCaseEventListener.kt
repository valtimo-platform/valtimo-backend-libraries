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

package com.ritense.case_.listener

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.case_.domain.tab.CaseWidgetTabWidgetId
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionPreDeleteEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional
@Component
@SkipComponentScan
class CaseTabCaseEventListener(
    private val service: CaseTabService,
    private val caseWidgetTabRepository: CaseWidgetTabRepository,
) {

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionCreatedEvent::class)
    fun handleCaseDefinitionCreatedEvent(event: CaseDefinitionCreatedEvent) {
        if (event.basedOnCaseDefinitionId != null) {
            service.getCaseTabs(event.basedOnCaseDefinitionId!!).forEach { oldCaseTab ->
                val caseTab = service.createCaseTab(event.caseDefinitionId, CaseTabDto.of(oldCaseTab))

                if (caseTab.type == CaseTabType.WIDGETS) {
                    caseWidgetTabRepository.findById(oldCaseTab.id).ifPresent { widgetTab ->
                        val newCaseWidget = caseWidgetTabRepository.save(
                            widgetTab.copy(
                                id = caseTab.id,
                                widgets = widgetTab.widgets.map {
                                    it.copy(id = CaseWidgetTabWidgetId(key = it.id.key, caseWidgetTab = widgetTab))
                                }
                            )
                        )
                        caseWidgetTabRepository.save(newCaseWidget)
                    }
                }
            }
        }
    }

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionPreDeleteEvent::class)
    fun handleCaseDefinitionPreDeleteEvent(event: CaseDefinitionPreDeleteEvent) {
        service.deleteCaseTabs(event.caseDefinitionId)
    }
}