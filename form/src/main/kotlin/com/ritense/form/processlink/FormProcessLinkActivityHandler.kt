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

package com.ritense.form.processlink

import com.ritense.form.domain.FormProcessLink
import com.ritense.form.domain.FormTaskOpenResultProperties
import com.ritense.form.service.PrefillFormService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import org.camunda.bpm.engine.task.Task
import java.util.UUID

class FormProcessLinkActivityHandler(
    private val formDefinitionService: FormIoFormDefinitionService,
    private val prefillFormService: PrefillFormService,
) : ProcessLinkActivityHandler<FormTaskOpenResultProperties> {

    override fun supports(processLink: ProcessLink): Boolean {
        return processLink is FormProcessLink
    }

    override fun openTask(task: Task, processLink: ProcessLink): ProcessLinkActivityResult<FormTaskOpenResultProperties> {
        processLink as FormProcessLink
        val formDefinition = prefillFormService.getPrefilledFormDefinition(
            formDefinitionId = processLink.formDefinitionId,
            processInstanceId = task.processInstanceId,
            taskInstanceId = task.id,
        )
        return ProcessLinkActivityResult(
            FORM_TASK_TYPE_KEY,
            FormTaskOpenResultProperties(processLink.formDefinitionId, formDefinition.asJson())
        )
    }

    override fun getStartEventObject(
        processDefinitionId: String,
        documentId: UUID?,
        documentDefinitionName: String?,
        processLink: ProcessLink
    ): ProcessLinkActivityResult<FormTaskOpenResultProperties> {
        processLink as FormProcessLink
        val formDefinition = prefillFormService.getPrefilledFormDefinition(processLink.formDefinitionId, documentId)
        return ProcessLinkActivityResult(
            FORM_TASK_TYPE_KEY,
            FormTaskOpenResultProperties(processLink.formDefinitionId, formDefinition.asJson())
        )
    }

    companion object {
        private const val FORM_TASK_TYPE_KEY = "form"
    }
}
