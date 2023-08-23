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

package com.ritense.valtimo.formflow

import com.ritense.document.service.DocumentService
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.service.FormFlowService
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import com.ritense.valtimo.formflow.domain.FormFlowProcessLink
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.task.Task
import java.util.UUID

class FormFlowProcessLinkActivityHandler(
    private val formFlowService: FormFlowService,
    private val repositoryService: RepositoryService,
    documentService: DocumentService,
    runtimeService: RuntimeService
) : AbstractFormFlowLinkTaskProvider(
    documentService,
    runtimeService
), ProcessLinkActivityHandler<FormFlowTaskOpenResultProperties> {

    override fun supports(processLink: ProcessLink): Boolean {
        return processLink is FormFlowProcessLink
    }

    override fun openTask(task: Task, processLink: ProcessLink): ProcessLinkActivityResult<FormFlowTaskOpenResultProperties> {
        processLink as FormFlowProcessLink

        val instances = formFlowService.findInstances(mapOf("taskInstanceId" to task.id))
        val instance = when (instances.size) {
            0 -> createFormFlowInstance(task, processLink)
            1 -> instances[0]
            else -> throw IllegalStateException("Multiple form flow instances linked to task: ${task.id}")
        }
        return ProcessLinkActivityResult(processLink.id, FORM_FLOW_TASK_TYPE_KEY, FormFlowTaskOpenResultProperties(instance.id.id))
    }

    override fun getStartEventObject(
        processDefinitionId: String,
        documentId: UUID?,
        documentDefinitionName: String?,
        processLink: ProcessLink,
        tenantId: String
    ): ProcessLinkActivityResult<FormFlowTaskOpenResultProperties> {
        processLink as FormFlowProcessLink
        val formFlowDefinition = formFlowService.findDefinition(processLink.formFlowDefinitionId)!!
        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(processDefinitionId)
            .singleResult()

        val additionalProperties = mutableMapOf<String, Any>("processDefinitionKey" to processDefinition.key)
        documentId?.let { additionalProperties["documentId"] = it }
        documentDefinitionName?.let { additionalProperties["documentDefinitionName"] = it }

        return ProcessLinkActivityResult(processLink.id, FORM_FLOW_TASK_TYPE_KEY, FormFlowTaskOpenResultProperties(
            formFlowService.save(
                formFlowDefinition.createInstance(additionalProperties)
            ).id.id)
        )
    }

    private fun createFormFlowInstance(task: Task, processLink: FormFlowProcessLink): FormFlowInstance {
        val additionalProperties = getAdditionalProperties(task)
        val formFlowDefinition = formFlowService.findDefinition(processLink.formFlowDefinitionId)!!
        return formFlowService.save(formFlowDefinition.createInstance(additionalProperties))
    }

}
