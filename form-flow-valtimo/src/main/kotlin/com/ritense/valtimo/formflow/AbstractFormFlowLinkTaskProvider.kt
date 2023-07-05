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

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.exception.DocumentNotFoundException
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.camunda.bpm.engine.RuntimeService

abstract class AbstractFormFlowLinkTaskProvider(
    private val documentService: DocumentService,
    private val runtimeService: RuntimeService,
) {

    protected fun getAdditionalProperties(task: CamundaTask): Map<String, Any> {
        val processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(task.getProcessInstanceId())
            .singleResult()

        val additionalProperties = mutableMapOf(
            "processInstanceId" to task.getProcessInstanceId(),
            "processInstanceBusinessKey" to processInstance.businessKey,
            "taskInstanceId" to task.id
        )

        try {
            val document = AuthorizationContext.runWithoutAuthorization { documentService[processInstance.businessKey] }
            if (document != null) {
                additionalProperties["documentId"] = processInstance.businessKey
            }
        } catch (e: DocumentNotFoundException) {
            // we do nothing here, intentional
        }

        return additionalProperties
    }

    companion object {
        const val FORM_FLOW_TASK_TYPE_KEY = "form-flow"
    }

}
