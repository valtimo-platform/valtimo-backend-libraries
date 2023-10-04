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

package com.ritense.valtimo.camunda.service

import java.util.concurrent.Callable
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.context.ProcessEngineContextImpl

class CamundaContextService(
    processEngineConfiguration: ProcessEngineConfigurationImpl
) {
    init {
        Companion.processEngineConfiguration = processEngineConfiguration
    }

    companion object {
        private lateinit var processEngineConfiguration: ProcessEngineConfigurationImpl

        internal fun <T> runWithCommandContext(callable: Callable<T>): T {
            val isNew = ProcessEngineContextImpl.consume()
            try {
                val context = processEngineConfiguration.commandContextFactory.createCommandContext()
                Context.setCommandContext(context)
                Context.setProcessEngineConfiguration(processEngineConfiguration)
                return callable.call()
            } finally {
                Context.removeCommandContext()
                Context.removeProcessEngineConfiguration()
                ProcessEngineContextImpl.set(isNew)
            }
        }
    }
}