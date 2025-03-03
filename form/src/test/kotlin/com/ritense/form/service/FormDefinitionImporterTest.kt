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

package com.ritense.form.service

import com.ritense.form.autodeployment.FormDefinitionDeploymentService
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class FormDefinitionImporterTest(
    @Mock private val formDefinitionDeploymentService: FormDefinitionDeploymentService
) {
    private lateinit var importer: FormDefinitionImporter

    @BeforeEach
    fun before() {
        importer = FormDefinitionImporter(formDefinitionDeploymentService)
    }

    @Test
    fun `should be of type 'form'`() {
        assertThat(importer.type()).isEqualTo("form")
    }

    @Test
    fun `should depend on case definition`() {
        assertThat(importer.dependsOn().toString()).isEqualTo(listOf(CASE_DEFINITION).toString())
    }


    @Test
    fun `should support form fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-form fileName`() {
        assertThat(importer.supports("config/case/person/1-0-0/form/not/test.json")).isFalse()
        assertThat(importer.supports("config/form/test.json")).isFalse()
        assertThat(importer.supports("config/case/person/1-0-0/form/test-json")).isFalse()
    }

    private companion object {
        const val FILENAME = "/form/my-form.json"
    }
}