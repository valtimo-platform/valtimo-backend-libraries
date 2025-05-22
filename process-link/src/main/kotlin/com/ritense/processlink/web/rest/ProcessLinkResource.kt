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
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.domain.ProcessLinkType
import com.ritense.processlink.mapper.ProcessLinkMapper
import com.ritense.processlink.service.ProcessDeploymentService
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.processlink.web.rest.dto.CaseProcessDefinitionResponseDto
import com.ritense.processlink.web.rest.dto.ProcessDefinitionResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valtimo.web.rest.dto.ProcessDefinitionWithPropertiesDto
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.util.IoUtil
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
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.stream.Collectors

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class ProcessLinkResource(
    private var processLinkService: ProcessLinkService,
    private val processLinkMappers: List<ProcessLinkMapper>,
    private val camundaProcessService: CamundaProcessService,
    private val processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService,
    private val repositoryService: RepositoryService,
    private val processDeploymentService: ProcessDeploymentService
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
            // To
            processLinkService.createProcessLink(processLink, null)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }
    }

    @PutMapping("/v1/process-link")
    fun updateProcessLink(
        @RequestBody processLink: ProcessLinkUpdateRequestDto
    ): ResponseEntity<Unit> {
        return withLoggingContext(ProcessLink::class, processLink.id) {
            processLinkService.updateProcessLink(processLink, null)
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

    @GetMapping(
        value = ["/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/process-definition"],
    )
    @Transactional
    fun getProcessDefinitionsAndProcessLinks(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String
    ): ResponseEntity<List<CaseProcessDefinitionResponseDto>> {
        val definitions = runWithoutAuthorization {
            camundaProcessService
                .getDeployedDefinitions(CaseDefinitionId.of(caseDefinitionKey, versionTag))
                .stream()
                .map { definition: CamundaProcessDefinition? ->
                    CaseProcessDefinitionResponseDto(
                        ProcessDefinitionWithPropertiesDto.fromProcessDefinition(
                            definition
                        ),
                        processDefinitionCaseDefinitionService.findByProcessDefinitionId(
                            ProcessDefinitionId(definition!!.id)
                        ),
                        processLinkService.getProcessLinks(definition.id).map {
                            getProcessLinkMapper(it.processLinkType).toProcessLinkResponseDto(it)
                        },
                        String(
                            IoUtil.readInputStream(
                                repositoryService.getProcessModel(definition.id),
                                "processModelBpmn20Xml"
                            ), StandardCharsets.UTF_8
                        )
                    )
                }
                .collect(Collectors.toList())
        }

        return ResponseEntity.ok(definitions)
    }

    @GetMapping("/management/v1/process-definition")
    @Transactional
    fun getUnlinkedProcessDefinitionsAndProcessLinks(): ResponseEntity<List<ProcessDefinitionResponseDto>> {
        val definitions = runWithoutAuthorization {
            camundaProcessService
                .getUnlinkedDeployedDefinitions()
                .stream()
                .map { definition ->
                    ProcessDefinitionResponseDto(
                        ProcessDefinitionWithPropertiesDto.fromProcessDefinition(definition),
                        processLinkService.getProcessLinks(definition.id).map {
                            getProcessLinkMapper(it.processLinkType).toProcessLinkResponseDto(it)
                        },
                        String(
                            IoUtil.readInputStream(
                                repositoryService.getProcessModel(definition.id),
                                "processModelBpmn20Xml"
                            ), StandardCharsets.UTF_8
                        )
                    )
                }
                .collect(Collectors.toList())
        }

        return ResponseEntity.ok(definitions)
    }

    @GetMapping("/management/v1/process-definition/key/{processDefinitionKey}")
    @Transactional
    fun getUnlinkedProcessDefinitionsByKeyList(
        @PathVariable("processDefinitionKey") processDefinitionKey: String
    ): ResponseEntity<List<ProcessDefinitionResponseDto>> {
        val definitions = runWithoutAuthorization {
            camundaProcessService.getUnlinkedDeployedDefinitionsByKey(processDefinitionKey)
        }

        val responseDtos = definitions.map { definition ->
            ProcessDefinitionResponseDto(
                ProcessDefinitionWithPropertiesDto.fromProcessDefinition(definition),
                processLinkService.getProcessLinks(definition.id).map {
                    getProcessLinkMapper(it.processLinkType).toProcessLinkResponseDto(it)
                },
                String(
                    IoUtil.readInputStream(
                        repositoryService.getProcessModel(definition.id),
                        "processModelBpmn20Xml"
                    ), StandardCharsets.UTF_8
                )
            )
        }.sortedBy { it.processDefinition.version }

        return ResponseEntity.ok(responseDtos)
    }


    @GetMapping(
        value = ["/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/process-definition/{processDefinitionId}"],
    )
    @Transactional
    fun getSingleProcessDefinitionWithLinks(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @PathVariable("processDefinitionId") processDefinitionId: String
    ): ResponseEntity<CaseProcessDefinitionResponseDto> {
        val definition = camundaProcessService.getProcessDefinitionById(processDefinitionId)

        val responseDto = CaseProcessDefinitionResponseDto(
            ProcessDefinitionWithPropertiesDto.fromProcessDefinition(definition),
            processDefinitionCaseDefinitionService.findByProcessDefinitionId(
                ProcessDefinitionId(definition.id)
            ),
            processLinkService.getProcessLinks(definition.id).map {
                getProcessLinkMapper(it.processLinkType).toProcessLinkResponseDto(it)
            },
            String(
                IoUtil.readInputStream(
                    repositoryService.getProcessModel(definition.id),
                    "processModelBpmn20Xml"
                ), StandardCharsets.UTF_8
            )
        )

        return ResponseEntity.ok(responseDto)
    }

    @GetMapping("/management/v1/process-definition/{processDefinitionKey}")
    @Transactional
    fun getUnlinkedProcessDefinitionsWithLinks(
        @PathVariable("processDefinitionKey") processDefinitionKey: String
    ): ResponseEntity<List<ProcessDefinitionResponseDto>> {
        val definitions = camundaProcessService.getDefinitionsByKey(processDefinitionKey)

        val responseDto = definitions.map {
            ProcessDefinitionResponseDto(
                ProcessDefinitionWithPropertiesDto.fromProcessDefinition(it),
                processLinkService.getProcessLinks(it.id).map {
                    getProcessLinkMapper(it.processLinkType).toProcessLinkResponseDto(it)
                },
                String(
                    IoUtil.readInputStream(
                        repositoryService.getProcessModel(it.id),
                        "processModelBpmn20Xml"
                    ), StandardCharsets.UTF_8
                )
            )
        }.sortedBy { it.processDefinition.version }

        return ResponseEntity.ok(responseDto)
    }

    @GetMapping(
        value = ["/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/process-definition/key/{processDefinitionKey}"]
    )
    @Transactional
    fun getProcessDefinitionByKeyWithLinks(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @PathVariable("processDefinitionKey") processDefinitionKey: String
    ): ResponseEntity<CaseProcessDefinitionResponseDto> {
        val caseDefinitionId = CaseDefinitionId.of(caseDefinitionKey, versionTag)

        val definition = runWithoutAuthorization {
            camundaProcessService
                .getDefinitionsByKeyAndCaseDefinition(caseDefinitionId, processDefinitionKey)
                .firstOrNull()
                ?: throw IllegalStateException("No process definition found for key '$processDefinitionKey' in case definition '$caseDefinitionId'")
        }

        val responseDto = CaseProcessDefinitionResponseDto(
            ProcessDefinitionWithPropertiesDto.fromProcessDefinition(definition),
            processDefinitionCaseDefinitionService.findByProcessDefinitionId(
                ProcessDefinitionId(definition.id)
            ),
            processLinkService.getProcessLinks(definition.id).map {
                getProcessLinkMapper(it.processLinkType).toProcessLinkResponseDto(it)
            },
            String(
                IoUtil.readInputStream(
                    repositoryService.getProcessModel(definition.id),
                    "processModelBpmn20Xml"
                ), StandardCharsets.UTF_8
            )
        )

        return ResponseEntity.ok(responseDto)
    }

    @DeleteMapping(
        value = ["/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/process-definition/key/{processDefinitionKey}"],
    )
    @Transactional
    fun deleteProcessDefinitionsAndProcessLinks(
        @PathVariable("caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable("versionTag") versionTag: String,
        @PathVariable("processDefinitionKey") processDefinitionKey: String,
    ): ResponseEntity<Any> {
        runWithoutAuthorization {
            camundaProcessService
                .getDefinitionsByKeyAndCaseDefinition(
                    CaseDefinitionId.of(caseDefinitionKey, versionTag),
                    processDefinitionKey
                )
                .forEach { definition: CamundaProcessDefinition ->
                    processDefinitionCaseDefinitionService.deleteProcessDefinitionCaseDefinition(
                        ProcessDefinitionId(definition.id),
                        CaseDefinitionId.of(caseDefinitionKey, versionTag)
                    )
                    processLinkService.deleteProcessLinksForProcessDefinition(definition.id)
                    camundaProcessService.deleteProcessDefinition(definition.id)
                }
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @DeleteMapping("/management/v1/process-definition/key/{processDefinitionKey}")
    @Transactional
    fun deleteUnlinkedProcessDefinitionsAndLinksByKey(
        @PathVariable("processDefinitionKey") processDefinitionKey: String
    ): ResponseEntity<Any> {
        runWithoutAuthorization {
            camundaProcessService.getDefinitionsByKey(processDefinitionKey)
                .forEach { definition ->
                    processLinkService.deleteProcessLinksForProcessDefinition(definition.id)
                    camundaProcessService.deleteProcessDefinition(definition.id)
                }
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping(
        value = ["/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/process-definition"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Transactional
    fun deployProcessDefinitionAndProcessLinks(
        @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @PathVariable(name = "caseDefinitionVersionTag") caseDefinitionVersionTag: String,
        @RequestPart(name = "file") bpmn: MultipartFile?,
        @RequestPart(name = "processLinks") processLinks: List<ProcessLinkCreateRequestDto>,
        @RequestPart(name = "processDefinitionId") processDefinitionId: String?,
        @RequestPart(name = "canInitializeDocument") canInitializeDocument: String = "false",
        @RequestPart(name = "startableByUser") startableByUser: String = "false"
    ): ResponseEntity<Any> {
        val caseDefinitionId = CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag)
        processDeploymentService.deployProcessDefinitionAndProcessLinksForCaseDefinition(
            caseDefinitionId,
            bpmn,
            processLinks,
            processDefinitionId,
            canInitializeDocument.toBoolean(),
            startableByUser.toBoolean()
        )

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping(
        value = ["/management/v1/process-definition"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Transactional
    fun deployUnlinkedProcessDefinitionAndProcessLinks(
        @RequestPart(name = "file") bpmn: MultipartFile?,
        @RequestPart(name = "processLinks") processLinks: List<ProcessLinkCreateRequestDto>,
        @RequestPart(name = "processDefinitionId") processDefinitionId: String?
    ): ResponseEntity<Any> {
        processDeploymentService.deployProcessDefinitionAndProcessLinks(
            null,
            bpmn,
            processLinks,
            processDefinitionId
        )

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }


    private fun getProcessLinkMapper(processLinkType: String): ProcessLinkMapper {
        return processLinkMappers.singleOrNull { it.supportsProcessLinkType(processLinkType) }
            ?: throw IllegalStateException("No ProcessLinkMapper found for processLinkType $processLinkType")
    }
}
