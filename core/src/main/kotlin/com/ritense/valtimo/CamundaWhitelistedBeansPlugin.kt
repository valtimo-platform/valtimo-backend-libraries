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
import org.camunda.bpm.engine.spring.SpringExpressionManager
import org.springframework.context.ApplicationContext

class CamundaWhitelistedBeansPlugin(
    private val processBeans: Map<String, Any>,
    private val applicationContext: ApplicationContext
) : AbstractProcessEnginePlugin() {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl?) {
        logger.info("Registering process beans...")
        requireNotNull(processEngineConfiguration) { "No process engine configuration found. Failed to register process beans." }

        val processBeansAny = processBeans as Map<Any, Any>
        processEngineConfiguration.beans = processBeansAny
        processEngineConfiguration.setExpressionManager(SpringExpressionManager(applicationContext, processBeansAny))

        logger.info("Successfully registered process beans.")
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}