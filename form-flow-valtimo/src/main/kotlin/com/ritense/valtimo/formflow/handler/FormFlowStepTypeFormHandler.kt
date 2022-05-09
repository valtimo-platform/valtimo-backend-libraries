package com.ritense.valtimo.formflow.handler

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.form.service.FormLoaderService
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.instance.FormFlowStepInstance
import com.ritense.formflow.handler.FormFlowStepTypeHandler
import com.ritense.formflow.service.FormFlowObjectMapper

class FormFlowStepTypeFormHandler(
    private val formLoaderService: FormLoaderService,
    private val objectMapper: FormFlowObjectMapper
) : FormFlowStepTypeHandler {

    override fun getType() = "form"

    override fun getMetadata(stepInstance: FormFlowStepInstance): JsonNode {
        val stepDefinitionType = stepInstance.definition.type
        assert(stepDefinitionType.name == "form")
        val formDefinitionName = (stepDefinitionType.properties as FormStepTypeProperties).definition

        val optionalMetadata = if (stepInstance.submissionData == null) {
            formLoaderService.getFormDefinitionByName(formDefinitionName)
        } else {
            val prefillData = objectMapper.get().readTree(stepInstance.submissionData)
            formLoaderService.getFormDefinitionByNamePreFilled(formDefinitionName, prefillData)
        }

        return optionalMetadata.orElseThrow { IllegalStateException("No FormDefinition found by name $formDefinitionName") }
    }

}
