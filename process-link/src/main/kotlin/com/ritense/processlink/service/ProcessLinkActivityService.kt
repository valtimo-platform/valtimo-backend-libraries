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

package com.ritense.processlink.service

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.exception.ProcessLinkNotFoundException
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byActive
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byId
import com.ritense.valtimo.service.CamundaTaskService
import mu.KotlinLogging
import java.util.UUID

open class ProcessLinkActivityService(
    private val processLinkService: ProcessLinkService,
    private val taskService: CamundaTaskService,
    private val processLinkActivityHandlers: List<ProcessLinkActivityHandler<*>>
) {
    fun openTask(taskId: UUID): ProcessLinkActivityResult<*> {
        val task = taskService.findTask(
            byId(taskId.toString())
                .and(byActive())
        )

        return processLinkService.getProcessLinks(task.getProcessDefinitionId(), task.taskDefinitionKey!!)
            .firstNotNullOfOrNull { processLink ->
                processLinkActivityHandlers.firstOrNull { provider -> provider.supports(processLink) }
                    ?.openTask(task, processLink)
            } ?: throw ProcessLinkNotFoundException("For task with id '$taskId'.")
    }

    fun getStartEventObject(
        processDefinitionId: String,
        documentId: UUID?,
        documentDefinitionName: String?
        ): ProcessLinkActivityResult<*>? {
        val processLink = processLinkService.getProcessLinksByProcessDefinitionIdAndActivityType(processDefinitionId,
            ActivityTypeWithEventName.START_EVENT_START) ?: return null
        var result: ProcessLinkActivityResult<*>? = null
        processLinkActivityHandlers.forEach {
            if(it.supports(processLink)){
                result = it.getStartEventObject(processDefinitionId, documentId, documentDefinitionName, processLink)
            }
        }
        return result
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
