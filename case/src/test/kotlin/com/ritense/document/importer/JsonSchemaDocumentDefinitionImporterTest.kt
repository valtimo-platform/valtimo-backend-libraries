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

import com.ritense.document.domain.impl.JsonSchema
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.json.MapperSingleton.get
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
        assertThat(importer.dependsOn()).isEqualTo(setOf(CASE_DEFINITION))
    }

    @Test
    fun `should support document definition fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support invalid document definition fileName`() {
        assertThat(importer.supports("/document/definition/not/my-definition.json")).isFalse()
        assertThat(importer.supports("/document/definition/my-definition-json")).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = """{"name":"my-definition"}"""
        val caseDefinitionId = CaseDefinitionId.of("my-definition", "1.0.0")
        importer.import(ImportRequest(FILENAME, jsonContent.toByteArray(), caseDefinitionId))

        val jsonCaptor = argumentCaptor<JsonSchema>()
        verify(documentDefinitionService).deploy(jsonCaptor.capture(), eq(caseDefinitionId))

        assertThat(jsonCaptor.firstValue.asJson()).isEqualTo(get().readTree(jsonContent))
    }

    private companion object {
        const val FILENAME = "/document/definition/my-definition.document-definition.json"
    }
}