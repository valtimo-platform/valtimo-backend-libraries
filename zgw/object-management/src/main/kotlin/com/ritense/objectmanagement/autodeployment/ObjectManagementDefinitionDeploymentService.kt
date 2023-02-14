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
package com.ritense.objectmanagement.autodeployment

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.domain.ObjectManagementConfigurationAutoDeploymentFinishedEvent
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objectmanagement.service.ObjectManagementService
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import java.io.IOException
import java.util.*
import kotlin.text.Charsets.UTF_8

class ObjectManagementDefinitionDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val objectManagementService: ObjectManagementService,
    private val objectManagementRepository: ObjectManagementRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @EventListener(ApplicationReadyEvent::class)
    fun deployAllFromResourceFiles() {
        logger.info("Deploying all object management configurations from {}", PATH)
        val resources = loadResources()

        for (resource in resources) {
            try {
                val objectManagement = jacksonObjectMapper().readValue<ObjectManagement>(IOUtils.toString(resource!!.getInputStream(), UTF_8))

                if (objectManagementRepository.findByObjecttypeId(objectManagement.objecttypeId) == null && objectManagementRepository.findByTitle(objectManagement.title) == null) {
                    objectManagementService.create(objectManagement)
                } else {
                    objectManagementService.update(objectManagement)
                }

               logger.info("Deployed object management configuration {}", objectManagement.title)
            } catch (e: IOException) {
                logger.error("Error while deploying object management configurations", e)
            }
        }

        applicationEventPublisher.publishEvent(ObjectManagementConfigurationAutoDeploymentFinishedEvent())
    }

    private fun loadResources(): Array<Resource?> {
        return try {
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH)
        } catch (ioe: IOException) {
            logger.error("Failed to load resources from " + PATH, ioe)
            arrayOfNulls(0)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(
            ObjectManagementDefinitionDeploymentService::class.java
        )
        const val PATH = "classpath*:config/objectmanagement/*.json"
    }
}
