/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.processlink.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.processdocument.domain.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.exception.BpmnParseException
import com.ritense.valtimo.service.OperatonProcessService
import org.operaton.bpm.engine.ParseException
import org.operaton.bpm.model.bpmn.Bpmn
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@Transactional
class ProcessDeploymentService(
    private val operatonProcessService: OperatonProcessService,
    private val processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService,
    private val processLinkService: ProcessLinkService,
) {
    //TODO: this code could use a refactor
    fun deployProcessDefinitionAndProcessLinksForCaseDefinition(
        caseDefinitionId: CaseDefinitionId,
        bpmn: MultipartFile?,
        processLinks: List<ProcessLinkCreateRequestDto>,
        processDefinitionId: String?,
        canInitializeDocument: Boolean,
        startableByUser: Boolean
    ) {
        val deployedProcessDefinitionId = deployProcessDefinitionAndProcessLinks(
            caseDefinitionId,
            bpmn,
            processLinks,
            processDefinitionId
        )

        runWithoutAuthorization {
            val processIdToUpdate = if (deployedProcessDefinitionId != null) {
                deployedProcessDefinitionId
            } else {
                val model = Bpmn.readModelFromStream(bpmn!!.inputStream)
                val previouslyDeployProcess = operatonProcessService.getExistingProcessForFile(caseDefinitionId, model)
                ProcessDefinitionId(previouslyDeployProcess.id)
            }

            processDefinitionCaseDefinitionService.createProcessDocumentDefinition(
                ProcessDocumentDefinitionRequest(
                    processDefinitionId = processIdToUpdate,
                    caseDefinitionId = caseDefinitionId,
                    canInitializeDocument = canInitializeDocument,
                    startableByUser = startableByUser
                )
            )
        }
    }

    fun deployProcessDefinitionAndProcessLinks(
        caseDefinitionId: CaseDefinitionId?,
        bpmn: MultipartFile?,
        processLinks: List<ProcessLinkCreateRequestDto>,
        processDefinitionId: String?
    ): ProcessDefinitionId? {
        val deployedProcessDefinitionId: String

        if (bpmn != null) {
            try {
                val deployment = runWithoutAuthorization {
                    operatonProcessService.deploy(
                        caseDefinitionId,
                        bpmn.originalFilename,
                        ByteArrayInputStream(bpmn.bytes),
                        true,
                        false
                    )
                }

                // If the deployment is null, the same xml was deployed before
                if (deployment == null) {
                    runWithoutAuthorization {
                        val model = Bpmn.readModelFromStream(bpmn!!.inputStream)
                        val previouslyDeployProcess =
                            operatonProcessService.getExistingProcessForFile(caseDefinitionId, model)
                        processLinkService.deleteProcessLinksForProcessDefinition(previouslyDeployProcess.id)
                        createProcessLinks(processLinks = processLinks, caseDefinitionId = caseDefinitionId)
                    }
                    return null
                }

                val deployedProcessDefinition = runWithoutAuthorization {
                    operatonProcessService.getProcessDefinitionByDeploymentId(deployment.id)
                }

                deployedProcessDefinitionId = deployedProcessDefinition.id
            } catch (e: ParseException) {
                throw BpmnParseException(e)
            }
        } else {
            try {
                val deployment = runWithoutAuthorization {
                    operatonProcessService.duplicateProcessDefinitionById(
                        caseDefinitionId,
                        processDefinitionId,
                        true,
                        true
                    )
                }

                if (deployment == null) {
                    return null
                }

                val deployedProcessDefinition = runWithoutAuthorization {
                    operatonProcessService.getProcessDefinitionByDeploymentId(deployment.id)
                }

                deployedProcessDefinitionId = deployedProcessDefinition.id
            } catch (e: Exception) {
                throw RuntimeException("Failed to duplicate process definition. Rolling back deployment.", e)
            }
        }
        createProcessLinks(processLinks, deployedProcessDefinitionId, caseDefinitionId)

        return ProcessDefinitionId(deployedProcessDefinitionId)
    }

    private fun createProcessLinks(
        processLinks: List<ProcessLinkCreateRequestDto>,
        deployedProcessDefinitionId: String? = null,
        caseDefinitionId: CaseDefinitionId? = null
    ) {
        try {
            processLinks.map { originalLink ->
                if (deployedProcessDefinitionId != null) {
                    copyWithNewProcessDefinitionId(originalLink, deployedProcessDefinitionId)
                } else {
                    originalLink
                }
            }.forEach { link ->
                runWithoutAuthorization {
                    processLinkService.createProcessLink(link, caseDefinitionId)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to create process links. Rolling back deployment.", e)
        }
    }

    private fun copyWithNewProcessDefinitionId(
        original: ProcessLinkCreateRequestDto,
        newProcessDefinitionId: String
    ): ProcessLinkCreateRequestDto {
        //TODO: see if there's a way to do this without reflection
        val originalClass = original::class
        val properties = originalClass.memberProperties
        val constructor = originalClass.primaryConstructor

        val args = properties.associate { prop ->
            prop.name to if (prop.name == "processDefinitionId") newProcessDefinitionId else prop.getter.call(original)
        }

        return constructor?.callBy(constructor.parameters.associateWith { args[it.name] }) as ProcessLinkCreateRequestDto
    }
}