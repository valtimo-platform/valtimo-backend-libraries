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

package com.ritense.processlink.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.logging.LoggableResource
import com.ritense.logging.withLoggingContext
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.domain.ProcessLinkType
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.exception.BpmnParseException
import com.ritense.valtimo.service.CamundaProcessService
import org.camunda.bpm.engine.ParseException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.util.UUID
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class ProcessLinkResource(
    private var processLinkService: ProcessLinkService,
    private val processLinkMappers: List<ProcessLinkMapper>,
    private val camundaProcessService: CamundaProcessService
) {

    @GetMapping("/v1/process-link")
    fun getProcessLinks(
        @LoggableResource(resourceType = CamundaProcessDefinition::class) @RequestParam("processDefinitionId") processDefinitionId: String,
        @RequestParam("activityId") activityId: String?
    ): ResponseEntity<List<ProcessLinkResponseDto>> {
        val list = if (activityId.isNullOrEmpty()) {
            processLinkService.getProcessLinks(processDefinitionId)
        } else {
            processLinkService.getProcessLinks(processDefinitionId, activityId)
        }.map { getProcessLinkMapper(it.processLinkType).toProcessLinkResponseDto(it) }

        return ResponseEntity.ok(list)
    }

    @GetMapping("/v1/process-link/types")
    fun getSupportedProcessLinkTypes(
        @RequestParam(name = "activityType") activityType: String
    ): ResponseEntity<List<ProcessLinkType>> {
        return ResponseEntity.ok(processLinkService.getSupportedProcessLinkTypes(activityType))
    }

    @PostMapping("/v1/process-link")
    fun createProcessLink(
        @RequestBody processLink: ProcessLinkCreateRequestDto
    ): ResponseEntity<Unit> {
        return withLoggingContext(CamundaProcessDefinition::class.java, processLink.processDefinitionId) {
            processLinkService.createProcessLink(processLink)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }
    }

    @PutMapping("/v1/process-link")
    fun updateProcessLink(
        @RequestBody processLink: ProcessLinkUpdateRequestDto
    ): ResponseEntity<Unit> {
        return withLoggingContext(ProcessLink::class, processLink.id) {
            processLinkService.updateProcessLink(processLink)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }
    }

    @DeleteMapping("/v1/process-link/{processLinkId}")
    fun deleteProcessLink(
        @LoggableResource(resourceType = ProcessLink::class) @PathVariable(name = "processLinkId") processLinkId: UUID
    ): ResponseEntity<Unit> {
        processLinkService.deleteProcessLink(processLinkId)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }


    @Deprecated("Since 12.7.0")
    @GetMapping("/v1/process-link/export")
    fun exportProcessLinks(
        @LoggableResource("processDefinitionKey") @RequestParam("processDefinitionKey") processDefinitionKey: String
    ): ResponseEntity<List<ProcessLinkExportResponseDto>> {
        val list = runWithoutAuthorization {
            processLinkService.getProcessLinksByProcessDefinitionKey(processDefinitionKey)
                .map { getProcessLinkMapper(it.processLinkType).toProcessLinkExportResponseDto(it) }
        }

        return ResponseEntity.ok(list)
    }

    @PostMapping(
        value = ["/v1/process/definition/deployment/process-link"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Transactional
    fun deployProcessDefinitionAndProcessLinks(
        @RequestPart(name = "file") bpmn: MultipartFile?,
        @RequestPart(name = "processLinks") processLinks: List<ProcessLinkCreateRequestDto>,
        @RequestPart(name = "processDefinitionId") processDefinitionId: String?
    ): ResponseEntity<Any> {
        val deployedProcessDefinitionId: String

        if (bpmn !== null) {
            val correctFileExtension = bpmn.originalFilename?.endsWith(".bpmn") == true

            if (!correctFileExtension) {
                return ResponseEntity.badRequest().body("Invalid file name. Must have '.bpmn' or '.dmn' suffix.")
            }

            try {
                val deployment = runWithoutAuthorization {
                    camundaProcessService.deploy(bpmn.originalFilename, ByteArrayInputStream(bpmn.bytes), true, false)
                }
                val deployedProcessDefinition = runWithoutAuthorization {
                    camundaProcessService.getProcessDefinitionByDeploymentId(deployment.id)
                }

                deployedProcessDefinitionId = deployedProcessDefinition.id
            } catch (e: ParseException) {
                throw BpmnParseException(e)
            }
        } else {
            try {
                val deployment = runWithoutAuthorization {
                    camundaProcessService.duplicateProcessDefinitionById(processDefinitionId, true, true)
                }
                val deployedProcessDefinition = runWithoutAuthorization {
                    camundaProcessService.getProcessDefinitionByDeploymentId(deployment.id)
                }

                deployedProcessDefinitionId = deployedProcessDefinition.id
            } catch (e: Exception) {
                throw RuntimeException("Failed to duplicate process definition. Rolling back deployment.", e)
            }
        }

        try {
            processLinks.map { originalLink ->
                copyWithNewProcessDefinitionId(originalLink, deployedProcessDefinitionId)
            }.forEach { runWithoutAuthorization { processLinkService.createProcessLink(it) } }
        } catch (e: Exception) {
            throw RuntimeException("Failed to create process links. Rolling back deployment.", e)
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    private fun copyWithNewProcessDefinitionId(
        original: ProcessLinkCreateRequestDto,
        newProcessDefinitionId: String
    ): ProcessLinkCreateRequestDto {
        val originalClass = original::class
        val properties = originalClass.memberProperties
        val constructor = originalClass.primaryConstructor

        val args = properties.associate { prop ->
            prop.name to if (prop.name == "processDefinitionId") newProcessDefinitionId else prop.getter.call(original)
        }

        return constructor?.callBy(constructor.parameters.associateWith { args[it.name] }) as ProcessLinkCreateRequestDto
    }

    private fun getProcessLinkMapper(processLinkType: String): ProcessLinkMapper {
        return processLinkMappers.singleOrNull { it.supportsProcessLinkType(processLinkType) }
            ?: throw IllegalStateException("No ProcessLinkMapper found for processLinkType $processLinkType")
    }
}
