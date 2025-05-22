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

package com.ritense.document.importer

import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
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
class JsonSchemaDocumentDefinitionImporterTest(
    @Mock private val documentDefinitionService: JsonSchemaDocumentDefinitionService
) {
    private lateinit var importer: JsonSchemaDocumentDefinitionImporter

    @BeforeEach
    fun before() {
        importer = JsonSchemaDocumentDefinitionImporter(documentDefinitionService)
    }

    @Test
    fun `should be of type 'form-definition'`() {
        assertThat(importer.type()).isEqualTo("documentdefinition")
    }

    @Test
    fun `should not depend on any type`() {
        assertThat(importer.dependsOn()).isEmpty()
    }

    @Test
    fun `should support document definition fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support invalid document definition fileName`() {
        assertThat(importer.supports("config/document/definition/not/my-definition.json")).isFalse()
        assertThat(importer.supports("config/document/definition/my-definition-json")).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = """{"name":"my-definition"}"""
        importer.import(ImportRequest(FILENAME, jsonContent.toByteArray()))

        val jsonCaptor = argumentCaptor<String>()
        verify(documentDefinitionService).deploy(jsonCaptor.capture())

        assertThat(jsonCaptor.firstValue).isEqualTo(jsonContent)
    }

    private companion object {
        const val FILENAME = "config/document/definition/my-definition.json"
    }
}