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

package com.ritense.formflow.listener

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.FormFlowStepId
import com.ritense.formflow.service.FormFlowService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionPreDeleteEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional
@Component
@SkipComponentScan
class FormFlowCaseEventListener(
    private val service: FormFlowService,
) {

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionCreatedEvent::class)
    fun handleCaseDefinitionCreatedEvent(event: CaseDefinitionCreatedEvent) {
        if (event.basedOnCaseDefinitionId != null) {
            service.getFormFlowDefinitions(event.basedOnCaseDefinitionId!!).forEach { oldFormFlowDefinition ->
                val newSteps = oldFormFlowDefinition.steps.map { step ->
                    step.copy(id = FormFlowStepId.create(step.id.key))
                }
                val newFormDefinition = oldFormFlowDefinition.copy(
                    id = FormFlowDefinitionId.newId(oldFormFlowDefinition.id.key, event.caseDefinitionId),
                    steps = newSteps.toSet()
                )
                service.save(newFormDefinition)
            }
        }
    }

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionPreDeleteEvent::class)
    fun handleCaseDefinitionPreDeleteEvent(event: CaseDefinitionPreDeleteEvent) {
        service.deleteAllByCaseDefinitionId(event.caseDefinitionId)
    }
}