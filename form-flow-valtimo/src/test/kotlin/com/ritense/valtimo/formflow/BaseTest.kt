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

import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.domain.definition.FormFlowDefinitionId
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.definition.configuration.FormFlowDefinition as FormFlowDefinitionConfig

abstract class BaseTest {
    fun readFileAsString(fileName: String): String = this::class.java.getResource(fileName)!!.readText(Charsets.UTF_8)

    fun getFormFlowDefinition(formFlowKey: String, formFlowJson: String): FormFlowDefinition {
        val mapper = jacksonObjectMapper()
        mapper.registerSubtypes(NamedType(FormStepTypeProperties::class.java, "form"))
        val config = mapper.readValue(formFlowJson, FormFlowDefinitionConfig::class.java)
        return config.toDefinition(FormFlowDefinitionId.newId(formFlowKey))
    }
}
