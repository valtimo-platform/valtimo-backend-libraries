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

import com.ritense.processlink.web.rest.dto.OpenTaskResult
import java.util.UUID
import mu.KotlinLogging
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task

open class ProcessLinkTaskService(
    private val processLinkService: ProcessLinkService,
    private val taskService: TaskService,
    private val processLinkTaskProviders: List<ProcessLinkTaskProvider<*>>
) {
    fun openTask(taskId: UUID): OpenTaskResult<*> {
        val task: Task = taskService
            .createTaskQuery()
            .taskId(taskId.toString())
            .active()
            .singleResult()

        return processLinkService.getProcessLinks(task.processDefinitionId, task.taskDefinitionKey)
            .firstNotNullOfOrNull { processLink ->
                processLinkTaskProviders.firstOrNull { provider -> provider.supports(processLink) }
                    ?.openTask(task, processLink)
            } ?: throw NoSuchElementException("Could not find ProcessLinkTaskProvider or ProcessLink related to task $taskId")
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}