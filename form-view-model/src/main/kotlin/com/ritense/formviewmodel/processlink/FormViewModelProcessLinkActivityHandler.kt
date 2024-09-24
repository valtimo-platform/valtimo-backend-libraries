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

package com.ritense.formviewmodel.processlink

import com.ritense.form.domain.FormProcessLink
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkActivityHandler
import com.ritense.processlink.web.rest.dto.ProcessLinkActivityResult
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import java.util.UUID

@Order(Ordered.HIGHEST_PRECEDENCE)
class FormViewModelProcessLinkActivityHandler(
    private val formDefinitionService: FormIoFormDefinitionService,
) : ProcessLinkActivityHandler<FormViewModelTaskOpenResultProperties> {

    override fun supports(processLink: ProcessLink): Boolean {
        return processLink is FormProcessLink && processLink.viewModelEnabled
    }

    override fun openTask(
        task: CamundaTask,
        processLink: ProcessLink
    ): ProcessLinkActivityResult<FormViewModelTaskOpenResultProperties> {
        processLink as FormProcessLink
        val formDefinition = formDefinitionService.getFormDefinitionById(processLink.formDefinitionId)
            .orElseThrow { RuntimeException("Form definition not found by id ${processLink.formDefinitionId}") }
        return ProcessLinkActivityResult(
            processLinkId = processLink.id,
            type = FORM_VIEW_MODEL_TASK_TYPE_KEY,
            properties = FormViewModelTaskOpenResultProperties(
                formDefinitionId = processLink.formDefinitionId,
                formDefinition = formDefinition.asJson(),
                formName = formDefinition.name,
                formDisplayType = processLink.formDisplayType,
                formSize = processLink.formSize,
            )
        )
    }

    override fun getStartEventObject(
        processDefinitionId: String,
        documentId: UUID?,
        documentDefinitionName: String?,
        processLink: ProcessLink
    ): ProcessLinkActivityResult<FormViewModelTaskOpenResultProperties> {
        processLink as FormProcessLink
        val formDefinition = formDefinitionService.getFormDefinitionById(processLink.formDefinitionId)
            .orElseThrow { RuntimeException("Form definition not found by id ${processLink.formDefinitionId}") }
        return ProcessLinkActivityResult(
            processLink.id,
            FORM_VIEW_MODEL_TASK_TYPE_KEY,
            FormViewModelTaskOpenResultProperties(
                formDefinitionId = processLink.formDefinitionId,
                formDefinition = formDefinition.asJson(),
                formName = formDefinition.name
            )
        )
    }

    companion object {
        private const val FORM_VIEW_MODEL_TASK_TYPE_KEY = "form-view-model"
    }
}
