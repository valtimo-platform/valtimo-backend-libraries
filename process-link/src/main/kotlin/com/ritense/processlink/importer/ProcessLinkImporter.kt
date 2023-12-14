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

package com.ritense.processlink.importer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.ritense.authorization.AuthorizationContext
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.service.ProcessLinkExistsException
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import mu.KLogger
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional

@Transactional
class ProcessLinkImporter(
    private val processLinkService: ProcessLinkService,
    private val repositoryService: CamundaRepositoryService,
    private val objectMapper: ObjectMapper
) : Importer {
    override fun type() = "processlink"

    override fun dependsOn(): Set<String> {
        return setOf("processdefinition") +
            processLinkService.getImporterDependsOnTypes()
    }

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val processDefinitionKey = FILENAME_REGEX.matchEntire(request.fileName)!!.groupValues[1]
        val processDefinitionId =  AuthorizationContext.runWithoutAuthorization {
            repositoryService.findLatestProcessDefinition(processDefinitionKey)?.id
                ?: throw IllegalStateException("Error while deploying '${request.fileName}'. Could not find Process definition with key '$processDefinitionKey'.")
        }

        val jsonTree = objectMapper.readTree(request.content.toString(Charsets.UTF_8))
        require(jsonTree is ArrayNode) { "Error while processing file ${request.fileName}. Expected root item to be an array!" }

        jsonTree.forEachIndexed { index, node ->
            require(node is ObjectNode) { "Error while processing file ${request.fileName}. Expected item at index $index to be an object!" }

            if (!node.has("processDefinitionId")) {
                node.set<ObjectNode>("processDefinitionId", TextNode.valueOf(processDefinitionId))
            }

            val deployDto = objectMapper.treeToValue<ProcessLinkDeployDto>(node)

            val processLinkDto = processLinkService.getProcessLinkMapper(deployDto.processLinkType)
                .toProcessLinkCreateRequestDto(deployDto)

            try {
                processLinkService.createProcessLink(processLinkDto)
            } catch (e: ProcessLinkExistsException) {
                if (e.contentsDiffer) {
                    logger.error { "${e.message} Skipping autodeployment." }
                }
            }
        }
    }

    private companion object {
        val logger: KLogger = KotlinLogging.logger {}
        val FILENAME_REGEX = """(?:.*\/)?(.+)\.processlink\.json""".toRegex()
    }
}