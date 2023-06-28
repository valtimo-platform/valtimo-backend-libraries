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
package com.ritense.processlink.autodeployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.ritense.processlink.service.ProcessLinkExistsException
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import mu.KLogger
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import java.io.IOException

open class ProcessLinkDeploymentApplicationReadyEventListener(
    private val resourceLoader: ResourceLoader,
    private val repositoryService: CamundaRepositoryService,
    private val processLinkService: ProcessLinkService,
    private val objectMapper: ObjectMapper
) {

    @EventListener(ApplicationReadyEvent::class)
    @Order(Ordered.LOWEST_PRECEDENCE) //Make sure everything else has been deployed before this listener runs
    open fun deployProcessLinks() {
        logger.info { "Deploying all process links from $PATH" }
        try {
            val resources = loadResources()
            for (resource in resources) {
                val processDefinitionId = getProcessDefinitionId(resource.filename!!)

                val processLinkCreateDtos = getProcessLinks(resource, processDefinitionId)

                processLinkCreateDtos.forEach {  processLinkDto ->
                    try {
                        processLinkService.createProcessLink(processLinkDto)
                    } catch (e: ProcessLinkExistsException) {
                        if (e.contentsDiffer) {
                            logger.error { "${e.message} Skipping autodeployment." }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error while deploying process-links" }
        }
    }

    private fun getProcessDefinitionId(fileName: String): String {
        val processDefinitionKey = fileName.substringBefore(".processlink.json")
        return repositoryService.findLatestProcessDefinition(processDefinitionKey)!!.id
    }

    private fun getProcessLinks(
        resource: Resource,
        processDefinitionId: String?
    ): List<ProcessLinkCreateRequestDto> {
        val jsonTree = objectMapper.readTree(resource.inputStream)
        require(jsonTree is ArrayNode) { "Error while processing file ${resource.filename}. Expected root item to be an array!" }

        val processLinkCreateDtos = jsonTree.mapIndexed { index, node ->
            require(node is ObjectNode) { "Error while processing file ${resource.filename}. Expected item at index $index to be an object!" }

            if (!node.has("processDefinitionId")) {
                node.set<ObjectNode>("processDefinitionId", TextNode.valueOf(processDefinitionId))
            }

            val deployDto = objectMapper.treeToValue<ProcessLinkDeployDto>(node)

            processLinkService.getProcessLinkMapper(deployDto.processLinkType)
                .toProcessLinkCreateRequestDto(deployDto)
        }

        return processLinkCreateDtos
    }

    @Throws(IOException::class)
    private fun loadResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResources(PATH)
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val PATH = "classpath*:**/*.processlink.json"
    }
}