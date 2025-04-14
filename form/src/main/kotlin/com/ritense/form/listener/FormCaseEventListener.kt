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

package com.ritense.form.listener

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.form.domain.request.CreateFormDefinitionRequest
import com.ritense.form.service.FormDefinitionService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionPreDeleteEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional
@Component
@SkipComponentScan
class FormCaseEventListener(
    private val service: FormDefinitionService,
) {

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionCreatedEvent::class)
    fun handleCaseDefinitionCreatedEvent(event: CaseDefinitionCreatedEvent) {
        if (event.duplicate) {
            service.getFormDefinitions(event.basedOnCaseDefinitionId!!).forEach { oldFormDefinition ->
                service.createFormDefinition(
                    event.caseDefinitionId, CreateFormDefinitionRequest(
                        oldFormDefinition.name,
                        oldFormDefinition.formDefinition.toString(),
                        oldFormDefinition.isReadOnly
                    )
                )
            }
        }
    }

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionPreDeleteEvent::class)
    fun handleCaseDefinitionPreDeleteEvent(event: CaseDefinitionPreDeleteEvent) {
        service.deleteAllFormDefinitions(event.caseDefinitionId)
    }
}