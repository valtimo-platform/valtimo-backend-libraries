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

import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.service.PluginService
import org.camunda.bpm.engine.ActivityTypes
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.extension.reactor.bus.CamundaSelector
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener
import org.springframework.transaction.annotation.Transactional

@CamundaSelector(type = ActivityTypes.CALL_ACTIVITY, event = ExecutionListener.EVENTNAME_START)
open class ProcessLinkCallActivityStartListener(
    private val pluginProcessLinkRepository: PluginProcessLinkRepository,
    private val pluginService: PluginService,
) : ReactorExecutionListener() {

    @Transactional
    override fun notify(execution: DelegateExecution) {
        val pluginProcessLinks = pluginProcessLinkRepository.findByProcessDefinitionIdAndActivityIdAndActivityType(
            execution.processDefinitionId,
            execution.currentActivityId,
            ActivityType.CALL_ACTIVITY_START
        )

        pluginProcessLinks.forEach { pluginProcessLink ->
            pluginService.invoke(execution, pluginProcessLink)
        }
    }
}
