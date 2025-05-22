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

package com.ritense.processdocument.importer

import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.PROCESS_DEFINITION
import com.ritense.processdocument.service.ProcessDocumentDeploymentService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class ProcessDocumentLinkImporterTest(
    @Mock val processDocumentDeploymentService: ProcessDocumentDeploymentService
) {
    private lateinit var processDocumentLinkImporter: ProcessDocumentLinkImporter

    @BeforeEach
    fun beforeEach() {
        processDocumentLinkImporter = ProcessDocumentLinkImporter(processDocumentDeploymentService)
    }

    @Test
    fun `should be of type 'processdocumentlink'`() {
        assertThat(processDocumentLinkImporter.type()).isEqualTo("processdocumentlink")
    }

    @Test
    fun `should depend on 'documentdefinition' and 'processdefinition' type`() {
        assertThat(processDocumentLinkImporter.dependsOn()).isEqualTo(setOf(DOCUMENT_DEFINITION, PROCESS_DEFINITION))
    }

    @Test
    fun `should support process-document-link fileName`() {
        assertThat(processDocumentLinkImporter.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-process-document-link fileName`() {
        assertThat(processDocumentLinkImporter.supports("config/process-document-link/aa/test.json")).isFalse()
        assertThat(processDocumentLinkImporter.supports("config/process-document-link/test-json")).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = "{}"
        processDocumentLinkImporter.import(ImportRequest(FILENAME, jsonContent.toByteArray()))

        val processDocumentLinkKeyCaptor = argumentCaptor<String>()
        val jsonCaptor = argumentCaptor<String>()

        verify(processDocumentDeploymentService).deploy(processDocumentLinkKeyCaptor.capture(), jsonCaptor.capture())

        assertThat(processDocumentLinkKeyCaptor.firstValue).isEqualTo("my-process-document-link")
        assertThat(jsonCaptor.firstValue).isEqualTo(jsonContent)
    }

    private companion object {
        const val FILENAME = "config/process-document-link/my-process-document-link.json"
    }
}