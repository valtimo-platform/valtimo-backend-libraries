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

import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.FormLink
import com.ritense.formlink.domain.ProcessLinkTaskProvider
import com.ritense.formlink.domain.TaskOpenResult
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import org.camunda.bpm.engine.task.Task

class FormFlowProcessLinkTaskProvider(
    val formFlowService: FormFlowService
): ProcessLinkTaskProvider<FormFlowTaskOpenResultProperties> {
    private val FORM_FLOW_TASK_TYPE_KEY = "form-flow"

    override fun supports(formLink: FormLink?): Boolean {
        return formLink is BpmnElementFormFlowIdLink
    }

    override fun getTaskResult(task: Task, formLink: FormLink): TaskOpenResult<FormFlowTaskOpenResultProperties> {
        val instances = formFlowService.findInstances(mapOf("taskInstanceId" to task.id))
        assert(instances.size == 1)
        return TaskOpenResult(FORM_FLOW_TASK_TYPE_KEY, FormFlowTaskOpenResultProperties(instances[0].id.id))
    }
}