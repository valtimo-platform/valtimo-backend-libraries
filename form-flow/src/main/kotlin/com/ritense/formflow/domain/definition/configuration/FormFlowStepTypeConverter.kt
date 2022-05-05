package com.ritense.formflow.domain.definition.configuration

import com.ritense.formflow.service.FormFlowObjectMapper
import javax.persistence.AttributeConverter

class FormFlowStepTypeConverter(
    private val objectMapper: FormFlowObjectMapper
): AttributeConverter<FormFlowStepType, String> {

    override fun convertToDatabaseColumn(stepTypeProperties: FormFlowStepType?): String? {
        return stepTypeProperties?.let {
            objectMapper.get().writeValueAsString(it)
        }
    }

    override fun convertToEntityAttribute(json: String?): FormFlowStepType? {
        return json?.let {
            objectMapper.get().readValue(it, FormFlowStepType::class.java)
        }
    }
}
