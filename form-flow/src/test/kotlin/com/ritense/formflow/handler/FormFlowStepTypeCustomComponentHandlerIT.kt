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

package com.ritense.formflow.handler

import com.ritense.formflow.service.FormFlowService
import com.ritense.formflow.BaseIntegrationTest
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FormFlowStepTypeCustomComponentHandlerIT: BaseIntegrationTest() {

    @Autowired
    lateinit var formFlowService: FormFlowService

    @Test
    fun `should find correct properties for custom angular type step`() {
        val formFlowInstance = formFlowService
            .findDefinition("custom-component-test:latest")!!
            .createInstance(emptyMap())

        val stepProperties = formFlowService.getTypeProperties(formFlowInstance.getCurrentStep())

        assertEquals(CustomComponentTypeProperties::class, stepProperties::class)

        val customComponentTypeProperties = stepProperties as CustomComponentTypeProperties

        assertEquals("my-component-definition", customComponentTypeProperties.id)
    }
}