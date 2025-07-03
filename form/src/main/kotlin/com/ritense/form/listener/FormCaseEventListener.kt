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
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.domain.request.CreateFormDefinitionRequest
import com.ritense.form.service.FormDefinitionService
import com.ritense.form.web.rest.dto.FormProcessLinkUpdateRequestDto
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.processlink.domain.ProcessLinksCopiedEvent
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.event.CaseDefinitionCreatedEvent
import com.ritense.valtimo.contract.event.CaseDefinitionPreDeleteEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
@Component
@SkipComponentScan
class FormCaseEventListener(
    private val formDefinitionService: FormDefinitionService,
    private val processLinkService: ProcessLinkService,
) {

    @EventListener(CaseDefinitionCreatedEvent::class)
    @RunWithoutAuthorization
    fun handleCaseDefinitionCreatedEvent(event: CaseDefinitionCreatedEvent) {
        if (!event.duplicate || event.copyFormDefinitionsAfterProcessLinks == true) return;

        val sourceId = event.basedOnCaseDefinitionId ?: return
        val targetId = event.caseDefinitionId

        copyFormDefinitions(sourceId, targetId)
    }

    @RunWithoutAuthorization
    @EventListener(CaseDefinitionPreDeleteEvent::class)
    fun handleCaseDefinitionPreDeleteEvent(event: CaseDefinitionPreDeleteEvent) {
        formDefinitionService.deleteAllFormDefinitions(event.caseDefinitionId)
    }

    @RunWithoutAuthorization
    @EventListener(ProcessLinksCopiedEvent::class)
    fun handleProcessLinksCopiedEvent(event: ProcessLinksCopiedEvent) {
        if (event.caseDefinitionId == null || event.basedOnCaseDefinitionId == null) return;

        val deployedFormDefinitions = formDefinitionService.getFormDefinitions(event.caseDefinitionId)

        // skip if form definitions have already been deployed for newly created process definition
        if (deployedFormDefinitions.isNotEmpty()) {
            return;
        }

        val formDefinitionIdMapping = copyFormDefinitions(
            event.basedOnCaseDefinitionId!!,
            event.caseDefinitionId!!
        )

        event.copiedProcessLinks.forEach { processLink ->
            if (processLink !is FormProcessLink) return

            val formProcessLinkUpdateRequest = FormProcessLinkUpdateRequestDto(
                processLink.id,
                formDefinitionIdMapping[processLink.formDefinitionId] ?: processLink.formDefinitionId,
                processLink.viewModelEnabled,
                processLink.formDisplayType,
                processLink.formSize,
                processLink.subtitles
            )

            processLinkService.updateProcessLink(
                formProcessLinkUpdateRequest,
                event.caseDefinitionId
            )
        }
    }

    private fun copyFormDefinitions(
        sourceCaseDefinitionId: CaseDefinitionId,
        targetCaseDefinitionId: CaseDefinitionId
    ): Map<UUID, UUID> {
        val formDefinitionIdMapping = mutableMapOf<UUID, UUID>()

        formDefinitionService.getFormDefinitions(sourceCaseDefinitionId).forEach { oldFormDefinition ->
            val newFormDefinition = formDefinitionService.createFormDefinition(
                targetCaseDefinitionId,
                CreateFormDefinitionRequest(
                    oldFormDefinition.name,
                    oldFormDefinition.formDefinition.toString(),
                    oldFormDefinition.isReadOnly
                )
            )
            formDefinitionIdMapping[oldFormDefinition.id] = newFormDefinition.id
        }

        return formDefinitionIdMapping
    }
}