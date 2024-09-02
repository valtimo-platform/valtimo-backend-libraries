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
package com.ritense.objectmanagement.autodeployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.domain.ObjectManagementConfigurationAutoDeploymentFinishedEvent
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Service
import java.io.IOException

@Service
@SkipComponentScan
class ObjectManagementDefinitionDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val objectManagementService: ObjectManagementService,
    private val objectManagementRepository: ObjectManagementRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
    private val environment: Environment
) {
    @EventListener(ApplicationReadyEvent::class)
    @Order(Ordered.LOWEST_PRECEDENCE-2)
    fun deployAllFromResourceFiles() {
        logger.info("Deploying all object management configurations from {}", PATH)
        val resources = loadResources()

        val objectManagementList = resources.map { resource ->
            require(resource != null)
            val objectManagementNode = objectMapper.readValue<ObjectNode>(resource.inputStream)
            try {
                val propertyValues = objectManagementNode.properties().map { it.key }.associateWith { key ->
                    getEnvVariableOrYamlPropertyOrDirectValue(
                        objectManagementNode[key].asText()
                            ?: objectManagementNode[key].asBoolean().toString()
                    )
                }
                val objectManagement = objectMapper.readValue<ObjectManagement>(
                    objectManagementNode.apply {
                        propertyValues.forEach { (key, value) ->
                            put(key, value)
                        }
                    }.toString()
                )

                if (
                    objectManagementRepository.findByObjecttypeId(objectManagement.objecttypeId) == null
                    && objectManagementRepository.findByTitle(objectManagement.title) == null
                ) {
                    objectManagementService.create(objectManagement)
                } else {
                    objectManagementService.update(objectManagement)
                }.also {
                    logger.info("Deployed object management configuration {}", objectManagement.title)
                }
            } catch (e: IOException) {
                throw RuntimeException("Error while deploying object management configurations", e)
            }
        }

        applicationEventPublisher.publishEvent(
            ObjectManagementConfigurationAutoDeploymentFinishedEvent().objectManagementAutoDeploymentFinishedEvent(
                objectManagementList
            )
        )
    }

    private fun loadResources(): Array<Resource?> {
        return try {
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH)
        } catch (ioe: IOException) {
            throw RuntimeException("Failed to load resources from $PATH", ioe)
        }
    }

    private fun getEnvVariableOrYamlPropertyOrDirectValue(value: String): String {
        return Regex("\\$\\{([^\\}]+)\\}").find(value)?.let { matchResult ->
            System.getenv(matchResult.groupValues[1]) ?: System.getProperty(matchResult.groupValues[1]) ?: environment.getProperty(matchResult.groupValues[1])
        } ?: value
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        const val PATH = "classpath*:config/objectmanagement/*.json"
    }
}
