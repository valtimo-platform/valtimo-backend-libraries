/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.formflow.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.service.DocumentService
import com.ritense.form.domain.FormDefinition
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.PrefillFormService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.instance.FormFlowStepInstance
import com.ritense.formflow.handler.FormFlowStepTypeHandler

class FormFlowStepTypeFormHandler(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val prefillFormService: PrefillFormService,
    private val documentService: DocumentService,
    private val objectMapper: ObjectMapper
) : FormFlowStepTypeHandler {

    override fun getType() = "form"

    override fun getTypeProperties(stepInstance: FormFlowStepInstance): FormTypeProperties {
        val formDefinition = getFormDefinition(stepInstance)
        prefillWithAdditionalData(formDefinition, stepInstance.instance.getAdditionalProperties())
        prefillWithSubmissionData(formDefinition, stepInstance)
        return FormTypeProperties(formDefinition.formDefinition)
    }

    private fun getFormDefinition(stepInstance: FormFlowStepInstance): FormIoFormDefinition {
        val stepDefinitionType = stepInstance.definition.type
        assert(stepDefinitionType.name == getType())
        val formDefinitionName = (stepDefinitionType.properties as FormStepTypeProperties).definition
        return formIoFormDefinitionService.getFormDefinitionByName(formDefinitionName)
            .orElseThrow { IllegalStateException("No FormDefinition found by name $formDefinitionName") }
    }

    private fun prefillWithSubmissionData(formDefinition: FormDefinition, stepInstance: FormFlowStepInstance) {
        formDefinition.preFill(objectMapper.readTree(stepInstance.instance.getSubmissionDataContext()))
    }

    private fun prefillWithAdditionalData(
        formDefinition: FormIoFormDefinition,
        additionalProperties: Map<String, Any>
    ) {

        val documentId = additionalProperties["documentId"] as String?
        val taskInstanceId = additionalProperties["taskInstanceId"] as String?

        if (documentId == null) {
            return
        }

        val document = AuthorizationContext.runWithoutAuthorization { documentService.get(documentId) }

        prefillFormService.prefillFormDefinition(
            formDefinition,
            document,
            null,
            taskInstanceId
        )
    }
}
