/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.DocumentService
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.instance.FormFlowInstance
import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.FormLink
import com.ritense.formlink.domain.ProcessLinkTaskProvider
import com.ritense.formlink.domain.TaskOpenResult
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.service.FormAssociationService
import org.apache.logging.log4j.util.Strings
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.task.Task

class FormFlowProcessLinkTaskProvider(
    private val formFlowService: FormFlowService,
    private val formAssociationService: FormAssociationService,
    private val documentService: DocumentService,
    private val repositoryService: RepositoryService,
    private val runtimeService: RuntimeService,
): ProcessLinkTaskProvider<FormFlowTaskOpenResultProperties> {

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
        val additionalProperties: MutableMap<String, Any> = getAdditionalProperties(task)
        val formFlowIdAsArray = getFormAssociationByTask(task).formLink.formFlowId.split(":")
        val formFlowDefinition: FormFlowDefinition = getFormFlowDefinition(formFlowIdAsArray)
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

    private fun getFormFlowDefinition(formFlowIdAsArray: List<String>): FormFlowDefinition {
        if (formFlowIdAsArray.size != 2) {
            throw IllegalArgumentException("Invalid Format found for formFlowId '${Strings.join(formFlowIdAsArray, ':' )}'. Form flow id must have format key:version")
        }
        return if (formFlowIdAsArray[1] == "latest") {
            formFlowService.findLatestDefinitionByKey(formFlowIdAsArray[0])!!
        } else {
            formFlowService.findDefinition(
                FormFlowDefinitionId(formFlowIdAsArray[0], formFlowIdAsArray[1].toLong())
            )
        }
    }

    private fun getAdditionalProperties(task: Task): MutableMap<String, Any> {
        val processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(task.processInstanceId)
            .singleResult()

        val additionalProperties: MutableMap<String, Any> = mutableMapOf(
            "processInstanceId" to task.processInstanceId,
            "processInstanceBusinessKey" to processInstance.businessKey,
            "taskInstanceId" to task.id
        )

        try {
            val document = documentService[processInstance.businessKey]
            if (document != null) {
                additionalProperties["documentId"] = processInstance.businessKey
            }
        } catch (e: DocumentNotFoundException) {
            // we do nothing here, intentional
        }

        return additionalProperties
    }

    companion object {
        private const val FORM_FLOW_TASK_TYPE_KEY = "form-flow"
    }
}
