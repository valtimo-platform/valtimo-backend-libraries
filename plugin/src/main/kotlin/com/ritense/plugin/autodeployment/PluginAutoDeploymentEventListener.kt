/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
 *
 */

package com.ritense.plugin.autodeployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.ritense.plugin.service.PluginService
import mu.KLogger
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Transactional
class PluginAutoDeploymentEventListener(
    private val resourceLoader: ResourceLoader,
    private val pluginService: PluginService,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    @Order(Ordered.LOWEST_PRECEDENCE-1)
    @EventListener(ApplicationReadyEvent::class)
    fun deployPluginConfigurations(){
        logger.info { "Deploying all plugins from $PATH" }
        try {
            val resources = loadResources()
            for (resource in resources) {
                try {
                    createPluginConfigurations(resource)
                } catch (e: Exception) {
                    logger.error(e) { "Error while deploying plugin" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error while deploying plugins" }
        }
    }

    private fun createPluginConfigurations(
        resource: Resource
    ) {
        val jsonTree = objectMapper.readTree(resource.inputStream)
        require(jsonTree is ArrayNode) { "Error while processing file ${resource.filename}. Expected root item to be an array!" }
        jsonTree.mapIndexed { index, node ->
            require(node is ObjectNode) { "Error while processing file ${resource.filename}. Expected item at index $index to be an object!" }

            val deployDto = objectMapper.treeToValue<PluginAutoDeploymentDto>(node)
            pluginService.deployPluginConfigurations(deployDto)
        }
    }

    @Throws(IOException::class)
    private fun loadResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResources(PATH)
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val PATH = "classpath*:**/*.pluginconfig.json"
    }

}