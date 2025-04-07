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
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionDeletedEvent
import com.ritense.zakenapi.service.ZaakTypeLinkService
import com.ritense.zakenapi.web.rest.request.CreateZaakTypeLinkRequest
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional
@Component
@SkipComponentScan
class ZaakTypeLinkCaseEventListener(
    private val zaakTypeLinkService: ZaakTypeLinkService,
) {

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionCreatedEvent::class)
    fun handleCaseDefinitionCreatedEvent(event: CaseDefinitionCreatedEvent) {
        if (event.basedOnCaseDefinitionId != null) {
            zaakTypeLinkService.get(event.basedOnCaseDefinitionId!!)?.let {
                zaakTypeLinkService.createZaakTypeLink(event.caseDefinitionId, CreateZaakTypeLinkRequest.of(it))
            }
        }
    }

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionDeletedEvent::class)
    fun handleCaseDefinitionDeletedEvent(event: CaseDefinitionDeletedEvent) {
        zaakTypeLinkService.deleteZaakTypeLinkBy(event.caseDefinitionId)
    }
}