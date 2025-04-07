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

package com.ritense.document.listener

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.case.service.CaseTabService
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionDeletedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional
@Component
@SkipComponentScan
class DocumentDefinitionCaseEventListener(
    private val service: DocumentDefinitionService,
) {

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionCreatedEvent::class)
    fun handleCaseDefinitionCreatedEvent(event: CaseDefinitionCreatedEvent) {
        if (event.basedOnCaseDefinitionId != null) {
            service.findByCaseDefinitionId(event.basedOnCaseDefinitionId!!).ifPresent { documentDefinition ->
                service.deploy(documentDefinition.schema().textValue(), event.caseDefinitionId)
            }
        }
    }

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionDeletedEvent::class)
    fun handleCaseDefinitionDeletedEvent(event: CaseDefinitionDeletedEvent) {
        service.findAllBy(event.caseDefinitionId).forEach { documentDefinition ->
            service.removeDocumentDefinition(documentDefinition.id.name(), documentDefinition.id.caseDefinitionId())
        }
    }
}