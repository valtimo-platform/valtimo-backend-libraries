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

package com.ritense.case.service

import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class CaseListImporterTest(
    @Mock private val caseListDeploymentService: CaseListDeploymentService
) {
    private lateinit var importer: CaseListImporter

    @BeforeEach
    fun before() {
        importer = CaseListImporter(caseListDeploymentService)
    }

    @Test
    fun `should be of type 'caselist'`() {
        assertThat(importer.type()).isEqualTo("caselist")
    }

    @Test
    fun `should depend on 'documentdefinition' type`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf(DOCUMENT_DEFINITION))
    }

    @Test
    fun `should support caselist fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-caselist fileName`() {
        assertThat(importer.supports("config/case/list/x/test.json")).isFalse()
        assertThat(importer.supports("config/case/list/test-json")).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = "{}"
        importer.import(ImportRequest(FILENAME, jsonContent.toByteArray()))

        val nameCaptor = argumentCaptor<String>()
        val jsonCaptor = argumentCaptor<String>()

        verify(caseListDeploymentService).deployColumns(nameCaptor.capture(), jsonCaptor.capture())

        assertThat(nameCaptor.firstValue).isEqualTo("my-case-list")
        assertThat(jsonCaptor.firstValue).isEqualTo(jsonContent)
    }

    private companion object {
        const val FILENAME = "config/case/list/my-case-list.json"
    }
}