/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.listener

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.processdocument.service.CaseDefinitionProcessLinkService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionPreDeleteEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Transactional
@Component
@SkipComponentScan
class ProcessDocumentLinkEventListener(
    private val caseDefinitionProcessLinkService: CaseDefinitionProcessLinkService
) {

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionCreatedEvent::class)
    fun handleCaseDefinitionCreatedEvent(event: CaseDefinitionCreatedEvent) {
        if (event.duplicate) {
            val processLinks =
                caseDefinitionProcessLinkService.getDocumentDefinitionProcessLinks(event.basedOnCaseDefinitionId!!)

            processLinks.forEach {
                caseDefinitionProcessLinkService.saveDocumentDefinitionProcessLink(
                    event.caseDefinitionId,
                    it.id.processDefinitionKey,
                    it.type
                )
            }
        }
    }

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionPreDeleteEvent::class)
    fun handleCaseDefinitionPreDeleteEvent(event: CaseDefinitionPreDeleteEvent) {
        caseDefinitionProcessLinkService.deleteDocumentDefinitionProcesses(event.caseDefinitionId)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}