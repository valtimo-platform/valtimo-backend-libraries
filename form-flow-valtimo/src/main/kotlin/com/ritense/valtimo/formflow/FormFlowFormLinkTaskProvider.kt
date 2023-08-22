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
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.FormLink
import com.ritense.formlink.domain.FormLinkTaskProvider
import com.ritense.formlink.domain.TaskOpenResult
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.service.FormAssociationService
import com.ritense.tenancy.TenantResolver
import org.apache.logging.log4j.util.Strings
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.task.Task

class FormFlowFormLinkTaskProvider(
    private val formFlowService: FormFlowService,
    private val formAssociationService: FormAssociationService,
    documentService: DocumentService,
    private val repositoryService: RepositoryService,
    runtimeService: RuntimeService,
    tenantResolver: TenantResolver
): AbstractFormFlowLinkTaskProvider(
    documentService, runtimeService, tenantResolver
), FormLinkTaskProvider<FormFlowTaskOpenResultProperties> {

    override fun supports(formLink: FormLink?): Boolean {
        return formLink is BpmnElementFormFlowIdLink
    }

    override fun getTaskResult(task: Task, formLink: FormLink): TaskOpenResult<FormFlowTaskOpenResultProperties> {
        val instances = formFlowService.findInstances(mapOf("taskInstanceId" to task.id))
        val instance = when (instances.size) {
            0 -> createFormFlowInstance(task)
            1 -> instances[0]
            else -> throw IllegalStateException("Multiple form flow instances linked to task: ${task.id}")
        }
        return TaskOpenResult(FORM_FLOW_TASK_TYPE_KEY, FormFlowTaskOpenResultProperties(instance.id.id))
    }

    private fun createFormFlowInstance(task: Task): FormFlowInstance {
        val additionalProperties = getAdditionalProperties(task)
        val formFlowId = getFormAssociationByTask(task).formLink.formFlowId
        val formFlowDefinition = formFlowService.findDefinition(formFlowId)!!
        return formFlowService.save(formFlowDefinition.createInstance(additionalProperties))
    }

    private fun getFormAssociationByTask(task: Task): FormAssociation {
        val processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(task.processDefinitionId)
            .singleResult()

        val formAssociation = formAssociationService.getFormAssociationByFormLinkId(
            processDefinition.key,
            task.taskDefinitionKey
        ).orElseThrow { IllegalStateException("No form association found. Process: '${processDefinition.key}', task: '${task.taskDefinitionKey}'") }

        if (formAssociation.formLink !is BpmnElementFormFlowIdLink) {
            throw IllegalStateException("Found form association is not of type 'BpmnElementFormFlowIdLink'. Type: ${formAssociation.formLink.javaClass.simpleName}, process: '${processDefinition.key}', task: '${task.taskDefinitionKey}'")
        }

        return formAssociation
    }
}
