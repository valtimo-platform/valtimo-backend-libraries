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
package com.ritense.processlink.autodeployment

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.importer.ImportRequest
import com.ritense.processlink.importer.ProcessLinkImporter
import java.io.IOException
import mu.KLogger
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils

open class ProcessLinkDeploymentApplicationReadyEventListener(
    private val resourceLoader: ResourceLoader,
    private val processLinkImporter: ProcessLinkImporter,
    private val objectMapper: ObjectMapper,
    private val environment: Environment
) {

    @EventListener(ApplicationReadyEvent::class)
    @Order(Ordered.LOWEST_PRECEDENCE) //Make sure everything else has been deployed before this listener runs
    open fun deployProcessLinks() {
        logger.info { "Deploying all process links from $PATH" }
        loadResources().forEach { resource ->
            try {
                val fileName = requireNotNull(resource.filename)
                logger.info { "Deploying process link from file '${fileName}'" }

                val processLinkNode = objectMapper.readValue<ArrayNode>(resource.inputStream)
                val resolvedProcessLinkNode = resolveProperties(processLinkNode)

                val importRequest = ImportRequest(fileName, objectMapper.writeValueAsBytes(resolvedProcessLinkNode))

                processLinkImporter.import(importRequest)
            } catch (e: Exception) {
                logger.error(e) { "Error while deploying process-link: '${resource.filename}'" }
            }
        }
    }

    @Throws(IOException::class)
    private fun loadResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResources(PATH)
    }

    private fun resolveProperties(array: ArrayNode?): ArrayNode {
        val result = objectMapper.createArrayNode()
        array?.forEach {
            result.add(resolveValue(it))
        }
        return result
    }

    private fun resolveValue(node: JsonNode?): JsonNode? {
        if (node != null) {
            if (node is ObjectNode) {
                val result = objectMapper.createObjectNode()
                node.fields().forEachRemaining {
                    result.replace(it.key, resolveValue(it.value))
                }
                return result
            } else if (node.isArray) {
                return objectMapper.createArrayNode().addAll(node.map { resolveValue(it) })
            } else if (node.isTextual) {
                var value = node.textValue()
                Regex("\\$\\{([^\\}]+)\\}").findAll(value)
                    .map { it.groupValues }
                    .forEach { (placeholder, placeholderValue) ->
                        val resolvedValue = environment.getProperty(placeholderValue)
                            ?: System.getenv(placeholderValue)
                            ?: System.getProperty(placeholderValue)
                            ?: throw IllegalStateException("Failed to find environment variable: '$placeholderValue'")
                        value = value.replace(placeholder, resolvedValue)
                    }
                return TextNode(value)
            }
        }
        return node
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val PATH = "classpath*:**/*.processlink.json"
    }
}