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

package com.ritense.formflow.importer

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.formflow.domain.definition.FormFlowDefinition
import com.ritense.formflow.service.FormFlowService
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.FORM
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.core.io.ResourceLoader

@ExtendWith(MockitoExtension::class)
class FormFlowDefinitionImporterTest(
    @Mock private val resourceLoader: ResourceLoader,
    @Mock private val formFlowService: FormFlowService,
    @Mock private val objectMapper: ObjectMapper
) {
    private lateinit var formFlowDefinitionImporter: FormFlowDefinitionImporter


    @BeforeEach
    fun before() {
        formFlowDefinitionImporter = FormFlowDefinitionImporter(
            resourceLoader,
            formFlowService,
            objectMapper
        )
    }

    @Test
    fun `should be of type 'formflow'`() {
        assertThat(formFlowDefinitionImporter.type()).isEqualTo("formflow")
    }

    @Test
    fun `should depend on 'form' type`() {
        assertThat(formFlowDefinitionImporter.dependsOn()).isEqualTo(setOf(FORM))
    }

    @Test
    fun `should support formflow fileName`() {
        assertThat(formFlowDefinitionImporter.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-formflow fileName`() {
        assertThat(formFlowDefinitionImporter.supports("config/form-flow/not-supported/test.json")).isFalse()
        assertThat(formFlowDefinitionImporter.supports("config/form-flow/test-json")).isFalse()
    }

    @Test
    fun `should deploy method for import with correct parameters`() {
        val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")
        val jsonContent = """
            {
                "startStep": "step1",
                "steps": [
                    {
                        "key": "step1",
                        "type": {
                            "name": "form",
                            "properties": {
                                "definition": "aandachtspunten-step1"
                            }
                        }
                    }
                ]
            }
        """.trimIndent()
        formFlowDefinitionImporter.import(ImportRequest(FILENAME, jsonContent.toByteArray(), caseDefinitionId))

        val formFlowDefinitionCaptor = argumentCaptor<FormFlowDefinition>()

        val formFlowKeyCaptor = argumentCaptor<String>()
        val caseDefinitionIdCaptor = argumentCaptor<CaseDefinitionId>()

        verify(formFlowService).findDefinitionOrNull(formFlowKeyCaptor.capture(), caseDefinitionIdCaptor.capture())
        verify(formFlowService).save(formFlowDefinitionCaptor.capture())

        assertThat(formFlowKeyCaptor.firstValue).isEqualTo("my-form")
        assertThat(caseDefinitionIdCaptor.firstValue).isEqualTo(caseDefinitionId)

        assertThat(formFlowDefinitionCaptor.firstValue.startStep).isEqualTo("step1")
        assertThat(formFlowDefinitionCaptor.firstValue.steps.size).isEqualTo(1)
    }

    private companion object {
        const val FILENAME = "/form-flow/my-form.json"
    }
}