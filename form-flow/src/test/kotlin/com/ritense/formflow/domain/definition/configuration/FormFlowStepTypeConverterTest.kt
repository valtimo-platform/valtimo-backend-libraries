package com.ritense.formflow.domain.definition.configuration

import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.definition.configuration.step.StepTypeProperties
import com.ritense.formflow.service.FormFlowObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FormFlowStepTypeConverterTest {

    private val formFlowStepTypeConverter = FormFlowStepTypeConverter(
        FormFlowObjectMapper(
            jacksonObjectMapper(),
            listOf(
                NamedType(FormStepTypeProperties::class.java, "form"),
                NamedType(TestFlowStepTypeProperties::class.java, "test")
            )
        )
    )

    @Test
    fun `should serialize form step type properties`() {
        val type = FormFlowStepType(
            name = "form",
            properties = FormStepTypeProperties(
                definition = "my-definition-name"
            )
        )
        val json = formFlowStepTypeConverter.convertToDatabaseColumn(type)
        assertThat(json).isEqualTo("""{"name":"form","properties":{"definition":"my-definition-name"}}""")
    }

    @Test
    fun `should deserialize form step type json`() {
        val type = formFlowStepTypeConverter.convertToEntityAttribute(
            """
            {
                "name": "form",
                "properties": {
                    "definition": "my-form-definition"
                }
            }
        """.trimIndent()
        )
        assertThat(type).isNotNull
        assertThat(type!!.properties).isInstanceOf(FormStepTypeProperties::class.java)
    }

    @Test
    fun `should deserialize test step type json`() {
        val type = formFlowStepTypeConverter.convertToEntityAttribute(
            """
            {
                "name": "test",
                "properties": {}
            }
        """.trimIndent()
        )
        assertThat(type).isNotNull
        assertThat(type!!.properties).isInstanceOf(TestFlowStepTypeProperties::class.java)
    }

    private class TestFlowStepTypeProperties: StepTypeProperties
}