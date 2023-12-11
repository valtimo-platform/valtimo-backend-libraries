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

package com.ritense.valtimo.formflow.importer

import com.ritense.formflow.service.FormFlowDeploymentService
import com.ritense.importer.ImportRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class FormFlowDefinitionImporterTest(
    @Mock private val formFlowDeploymentService: FormFlowDeploymentService
) {
    private lateinit var formFlowDefinitionImporter: FormFlowDefinitionImporter

    @BeforeEach
    fun before() {
        formFlowDefinitionImporter = FormFlowDefinitionImporter(formFlowDeploymentService)
    }

    @Test
    fun `should be of type 'formflow'`() {
        assertThat(formFlowDefinitionImporter.type()).isEqualTo("formflow")
    }

    @Test
    fun `should depend on 'form' type`() {
        assertThat(formFlowDefinitionImporter.dependsOn()).isEqualTo(setOf("form"))
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
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = "{}"
        formFlowDefinitionImporter.import(ImportRequest(FILENAME, jsonContent.toByteArray()))

        val formFlowKeyCaptor = argumentCaptor<String>()
        val jsonCaptor = argumentCaptor<String>()

        verify(formFlowDeploymentService).deploy(formFlowKeyCaptor.capture(), jsonCaptor.capture())

        assertThat(formFlowKeyCaptor.firstValue).isEqualTo("my-form")
        assertThat(jsonCaptor.firstValue).isEqualTo(jsonContent)
    }

    private companion object {
        const val FILENAME = "config/form-flow/my-form.json"
    }
}