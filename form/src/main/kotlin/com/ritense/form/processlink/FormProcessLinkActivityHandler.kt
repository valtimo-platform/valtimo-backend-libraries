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

package com.ritense.form.processlink

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.domain.FormTaskOpenResultProperties
import com.ritense.form.service.PrefillFormService
import com.ritense.logging.LoggableResource
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@SkipComponentScan
class FormProcessLinkActivityHandler(
    private val prefillFormService: PrefillFormService,
) : ProcessLinkActivityHandler<FormTaskOpenResultProperties> {

    override fun supports(processLink: ProcessLink): Boolean {
        return processLink is FormProcessLink && !processLink.viewModelEnabled
    }

    override fun openTask(
        task: CamundaTask,
        processLink: ProcessLink
    ): ProcessLinkActivityResult<FormTaskOpenResultProperties> {
        processLink as FormProcessLink
        val formDefinition = prefillFormService.getPrefilledFormDefinition(
            formDefinitionId = processLink.formDefinitionId,
            processInstanceId = task.getProcessInstanceId(),
            taskInstanceId = task.id,
        )
        return ProcessLinkActivityResult(
            processLink.id,
            FORM_TASK_TYPE_KEY,
            FormTaskOpenResultProperties(
                processLink.formDefinitionId,
                formDefinition.asJson(),
                processLink.formDisplayType,
                processLink.formSize,
            )
        )
    }

    override fun getStartEventObject(
        @LoggableResource(resourceType = CamundaProcessDefinition::class) processDefinitionId: String,
        @LoggableResource(resourceType = JsonSchemaDocument::class) documentId: UUID?,
        @LoggableResource("documentDefinitionName") documentDefinitionName: String?,
        processLink: ProcessLink
    ): ProcessLinkActivityResult<FormTaskOpenResultProperties> {
        processLink as FormProcessLink
        val formDefinition = prefillFormService.getPrefilledFormDefinition(processLink.formDefinitionId, documentId)
        return ProcessLinkActivityResult(
            processLink.id,
            FORM_TASK_TYPE_KEY,
            FormTaskOpenResultProperties(processLink.formDefinitionId, formDefinition.asJson())
        )
    }

    companion object {
        private const val FORM_TASK_TYPE_KEY = "form"
    }
}