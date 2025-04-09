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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.PROCESS_DEFINITION
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinition
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.processdocument.domain.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations.openMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ProcessDocumentLinkImporterTest(
    @Mock private val processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService,
    @Mock private val documentDefinitionService: DocumentDefinitionService,
    @Mock private val repositoryService: RepositoryService
) {
    private lateinit var processDocumentLinkImporter: ProcessDocumentLinkImporter

    @BeforeEach
    fun beforeEach() {
        openMocks(this)

        processDocumentLinkImporter = ProcessDocumentLinkImporter(
            processDefinitionCaseDefinitionService,
            documentDefinitionService,
            ObjectMapper(),
            repositoryService
        )
    }

    @Test
    fun `should be of type 'processdocumentlink'`() {
        assertThat(processDocumentLinkImporter.type()).isEqualTo("processdocumentlink")
    }

    @Test
    fun `should depend on 'documentdefinition' and 'processdefinition' type`() {
        assertThat(processDocumentLinkImporter.dependsOn()).isEqualTo(setOf(CASE_DEFINITION, PROCESS_DEFINITION))
    }

    @Test
    fun `should support process-document-link fileName`() {
        assertThat(processDocumentLinkImporter.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-process-document-link fileName`() {
        assertThat(processDocumentLinkImporter.supports("config/case/house/1-0-0/process-document-link/aa/test.json")).isFalse()
        assertThat(processDocumentLinkImporter.supports("config/case/house/1-0-0/process-document-link/test-json")).isFalse()
    }

    @Test
    fun `should import new record`() {
        val jsonContent = """
            [
                {
                    "processDefinitionKey": "test",
                    "canInitializeDocument": true,
                    "startableByUser": true
                }
            ]
        """.trimIndent()

        val documentDefinition = mock<JsonSchemaDocumentDefinition>()
        whenever(documentDefinitionService.findByNameAndCaseDefinitionId(any(), any()))
            .thenReturn(Optional.of(documentDefinition))

        val processDefinition = mock<ProcessDefinition>()
        val processDefinitionId = "test"
        val query = mock<ProcessDefinitionQuery>()
        whenever(repositoryService.createProcessDefinitionQuery()).thenReturn(query)
        whenever(query.processDefinitionKey(any())).thenReturn(query)
        whenever(query.versionTag(any())).thenReturn(query)
        whenever(query.singleResult()).thenReturn(processDefinition)
        whenever(processDefinition.id).thenReturn(processDefinitionId)

        whenever(processDefinitionCaseDefinitionService.findById(any()))
            .thenReturn(null)

        val caseDefinitionId = CaseDefinitionId("test", "1.0.0")
        processDocumentLinkImporter.import(ImportRequest(FILENAME, jsonContent.toByteArray(), caseDefinitionId))

        val processDocumentDefinitionRequestCaptor = argumentCaptor<ProcessDocumentDefinitionRequest>()

        verify(processDefinitionCaseDefinitionService).createProcessDocumentDefinition(processDocumentDefinitionRequestCaptor.capture())

        val request = processDocumentDefinitionRequestCaptor.firstValue
        assertThat(request.processDefinitionId.id).isEqualTo(processDefinitionId)
        assertThat(request.caseDefinitionId).isEqualTo(caseDefinitionId)
        assertThat(request.canInitializeDocument).isEqualTo(true)
        assertThat(request.startableByUser).isEqualTo(true)
    }

    @Test
    fun `should import existing record`() {
        val jsonContent = """
            [
                {
                    "processDefinitionKey": "test",
                    "canInitializeDocument": true,
                    "startableByUser": true
                }
            ]
        """.trimIndent()

        val documentDefinition = mock<JsonSchemaDocumentDefinition>()
        whenever(documentDefinitionService.findByNameAndCaseDefinitionId(any(), any()))
            .thenReturn(Optional.of(documentDefinition))

        val processDefinition = mock<ProcessDefinition>()
        val processDefinitionId = "test"
        val query = mock<ProcessDefinitionQuery>()
        whenever(repositoryService.createProcessDefinitionQuery()).thenReturn(query)
        whenever(query.processDefinitionKey(any())).thenReturn(query)
        whenever(query.versionTag(any())).thenReturn(query)
        whenever(query.singleResult()).thenReturn(processDefinition)
        whenever(processDefinition.id).thenReturn(processDefinitionId)

        val processDefinitionCaseDefinition = mock<ProcessDefinitionCaseDefinition>()
        whenever(processDefinitionCaseDefinitionService.findById(any()))
            .thenReturn(processDefinitionCaseDefinition)

        val caseDefinitionId = CaseDefinitionId("test", "1.0.0")
        processDocumentLinkImporter.import(ImportRequest(FILENAME, jsonContent.toByteArray(), caseDefinitionId))

        val processDocumentDefinitionRequestCaptor = argumentCaptor<ProcessDocumentDefinitionRequest>()

        verify(processDefinitionCaseDefinitionService).deleteProcessDocumentDefinition(
            ProcessDefinitionId(processDefinitionId),
            caseDefinitionId
        )
        verify(processDefinitionCaseDefinitionService).createProcessDocumentDefinition(processDocumentDefinitionRequestCaptor.capture())

        val request = processDocumentDefinitionRequestCaptor.firstValue
        assertThat(request.processDefinitionId.id).isEqualTo(processDefinitionId)
        assertThat(request.caseDefinitionId).isEqualTo(caseDefinitionId)
        assertThat(request.canInitializeDocument).isEqualTo(true)
        assertThat(request.startableByUser).isEqualTo(true)
    }

    private companion object {
        const val FILENAME = "/process-document-link/my-process-document-link.json"
    }
}