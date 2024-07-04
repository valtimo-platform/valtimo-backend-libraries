/*
 *  Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo

import mu.KotlinLogging
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl

class CamundaBeansPlugin : AbstractProcessEnginePlugin() {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl?) {
        requireNotNull(processEngineConfiguration) { "No process engine configuration found. Failed to register process beans." }

        // Register custom command interceptor to be able to use the beans without authorization
        processEngineConfiguration.setCustomPreCommandInterceptorsTxRequired(
            listOf(ValtimoCommandInterceptor())
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}