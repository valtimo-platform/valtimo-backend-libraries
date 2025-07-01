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

package com.ritense.formflow

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.DocumentService
import com.ritense.formflow.domain.FormFlowProcessLink
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.service.FormFlowService
import com.ritense.logging.LoggableResource
import com.ritense.logging.withLoggingContext
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import com.ritense.valtimo.operaton.domain.OperatonTask
import com.ritense.valtimo.operaton.service.OperatonRepositoryService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.operaton.bpm.engine.RuntimeService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@SkipComponentScan
class FormFlowProcessLinkActivityHandler(
    private val formFlowService: FormFlowService,
    private val repositoryService: OperatonRepositoryService,
    private val processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService,
    documentService: DocumentService,
    runtimeService: RuntimeService,
) : AbstractFormFlowLinkTaskProvider(
    documentService, runtimeService
), ProcessLinkActivityHandler<FormFlowTaskOpenResultProperties> {

    override fun supports(processLink: ProcessLink): Boolean {
        return processLink is FormFlowProcessLink
    }

    @Transactional
    override fun openTask(
        task: OperatonTask,
        processLink: ProcessLink
    ): ProcessLinkActivityResult<FormFlowTaskOpenResultProperties> {
        withLoggingContext(
            mapOf(
                JsonSchemaDocument::class.java.canonicalName to task.processInstance?.businessKey,
                OperatonTask::class.java.canonicalName to task.id,
                ProcessLink::class.java.canonicalName to processLink.id.toString()
            )
        ) {
            processLink as FormFlowProcessLink

            val instances = formFlowService.findInstances(mapOf("taskInstanceId" to task.id))
            val instance = when (instances.size) {
                0 -> createFormFlowInstance(task, processLink)
                else -> instances[0]
            }
            return ProcessLinkActivityResult(
                processLink.id,
                FORM_FLOW_TASK_TYPE_KEY,
                FormFlowTaskOpenResultProperties(
                    instance.id.id,
                    processLink.formDisplayType,
                    processLink.formSize,
                    processLink.subtitles
                )
            )
        }
    }

    override fun getStartEventObject(
        @LoggableResource(resourceType = OperatonProcessDefinition::class) processDefinitionId: String,
        @LoggableResource(resourceType = JsonSchemaDocument::class) documentId: UUID?,
        @LoggableResource("documentDefinitionName") documentDefinitionName: String?,
        processLink: ProcessLink
    ): ProcessLinkActivityResult<FormFlowTaskOpenResultProperties> {
        return withLoggingContext(ProcessLink::class, processLink.id) {
            processLink as FormFlowProcessLink
            val processDefinitionCaseDefinitionLink = processDefinitionCaseDefinitionService.findByProcessDefinitionId(ProcessDefinitionId(processDefinitionId))
            val formFlowDefinition = formFlowService.findDefinition(processLink.formFlowDefinitionKey, processDefinitionCaseDefinitionLink.id.caseDefinitionId)!!
            val processDefinition = AuthorizationContext.runWithoutAuthorization {
                repositoryService.findProcessDefinitionById(processDefinitionId)!!
            }

            val additionalProperties = mutableMapOf<String, Any>("processDefinitionKey" to processDefinition.key)
            documentId?.let { additionalProperties["documentId"] = it }
            documentDefinitionName?.let { additionalProperties["documentDefinitionName"] = it }

            ProcessLinkActivityResult(
                processLink.id,
                FORM_FLOW_TASK_TYPE_KEY,
                FormFlowTaskOpenResultProperties(
                    formFlowService.save(
                        formFlowDefinition.createInstance(additionalProperties)
                    ).id.id
                )
            )
        }
    }

    private fun createFormFlowInstance(task: OperatonTask, processLink: FormFlowProcessLink): FormFlowInstance {
        val additionalProperties = getAdditionalProperties(task)
        val processDefinitionCaseDefinitionLink = processDefinitionCaseDefinitionService
            .findByProcessDefinitionId(ProcessDefinitionId(processLink.processDefinitionId))

        val formFlowDefinition = formFlowService
            .findDefinition(processLink.formFlowDefinitionKey, processDefinitionCaseDefinitionLink.id.caseDefinitionId)!!
        return formFlowService.save(formFlowDefinition.createInstance(additionalProperties))
    }

}
