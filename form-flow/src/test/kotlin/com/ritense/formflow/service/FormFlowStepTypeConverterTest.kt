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

package com.ritense.formflow.domain.definition.configuration

import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.definition.configuration.step.StepTypeProperties
import com.ritense.formflow.service.ObjectMapperConfigurer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ObjectMapperConfigurerTest {

    private val mapper = jacksonObjectMapper()
    private val objectMapperConfigurer = ObjectMapperConfigurer(
        mapper,
        listOf(
            NamedType(FormStepTypeProperties::class.java, "form"),
            NamedType(TestFlowStepTypeProperties::class.java, "test")
        )
    ).apply {
        this.configure()
    }

    @Test
    fun `should serialize form step type properties`() {
        val type = FormFlowStepType(
            name = "form",
            properties = FormStepTypeProperties(
                definition = "my-definition-name"
            )
        )
        val json = mapper.writeValueAsString(type)
        assertThat(json).isEqualTo("""{"name":"form","properties":{"definition":"my-definition-name"}}""")
    }

    @Test
    fun `should deserialize form step type json`() {
        val type = mapper.readValue(
            """
                {
                    "name": "form",
                    "properties": {
                        "definition": "my-form-definition"
                    }
                }
            """.trimIndent(),
            FormFlowStepType::class.java
        )
        assertThat(type).isNotNull
        assertThat(type!!.properties).isInstanceOf(FormStepTypeProperties::class.java)
    }

    @Test
    fun `should deserialize test step type json`() {
        val type = mapper.readValue(
            """
                {
                    "name": "test",
                    "properties": {}
                }
            """.trimIndent(),
            FormFlowStepType::class.java
        )
        assertThat(type).isNotNull
        assertThat(type!!.properties).isInstanceOf(TestFlowStepTypeProperties::class.java)
    }

    private class TestFlowStepTypeProperties: StepTypeProperties
}