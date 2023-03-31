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
import com.ritense.processlink.service.ProcessLinkTaskProvider
import com.ritense.processlink.web.rest.dto.OpenTaskResult
import com.ritense.valtimo.formflow.domain.FormFlowProcessLink
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.task.Task

class FormFlowProcessLinkTaskProvider(
    private val formFlowService: FormFlowService,
    documentService: DocumentService,
    runtimeService: RuntimeService,
): AbstractFormFlowLinkTaskProvider(
    documentService, runtimeService
), ProcessLinkTaskProvider<FormFlowTaskOpenResultProperties> {

    override fun supports(processLink: ProcessLink): Boolean {
        return processLink is FormFlowProcessLink
    }

    override fun openTask(task: Task, processLink: ProcessLink): OpenTaskResult<FormFlowTaskOpenResultProperties> {
        processLink as FormFlowProcessLink

        val instances = formFlowService.findInstances(mapOf("taskInstanceId" to task.id))
        val instance = when (instances.size) {
            0 -> createFormFlowInstance(task, processLink)
            1 -> instances[0]
            else -> throw IllegalStateException("Multiple form flow instances linked to task: ${task.id}")
        }
        return OpenTaskResult(FORM_FLOW_TASK_TYPE_KEY, FormFlowTaskOpenResultProperties(instance.id.id))
    }

    private fun createFormFlowInstance(task: Task, processLink: FormFlowProcessLink): FormFlowInstance {
        val additionalProperties = getAdditionalProperties(task)
        val formFlowDefinition = formFlowService.findDefinition(processLink.formFlowDefinitionId)!!
        return formFlowService.save(formFlowDefinition.createInstance(additionalProperties))
    }

}
