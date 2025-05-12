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

package com.ritense.valtimo.processlink

import com.ritense.logging.withLoggingContext
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.service.PluginService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@SkipComponentScan
class ProcessLinkServiceTaskStartListener(
    private val pluginProcessLinkRepository: PluginProcessLinkRepository,
    private val pluginService: PluginService,
) {

    @Transactional
    @EventListener(
        condition = """#execution.bpmnModelElementInstance != null
            && #execution.bpmnModelElementInstance.elementType.typeName == T(org.camunda.bpm.engine.ActivityTypes).TASK_SERVICE
            && #execution.eventName == T(org.camunda.bpm.engine.delegate.ExecutionListener).EVENTNAME_START"""
    )
    fun notify(execution: DelegateExecution) {
        withLoggingContext("com.ritense.document.domain.impl.JsonSchemaDocument", execution.processBusinessKey) {
            val pluginProcessLinks = pluginProcessLinkRepository.findByProcessDefinitionIdAndActivityIdAndActivityType(
                execution.processDefinitionId,
                execution.currentActivityId,
                ActivityTypeWithEventName.SERVICE_TASK_START
            )

            pluginProcessLinks.forEach { pluginProcessLink ->
                pluginService.invoke(execution, pluginProcessLink)
            }
        }
    }
}
