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

package com.ritense.valtimo.importer

import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.service.OperatonProcessService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import java.io.ByteArrayInputStream

@ExtendWith(MockitoExtension::class)
class OperatonDecisionDefinitionImporterTest(
    @Mock private val operatonProcessService: OperatonProcessService
) {
    private lateinit var importer: OperatonDecisionDefinitionImporter

    @BeforeEach
    fun before() {
        importer = OperatonDecisionDefinitionImporter(operatonProcessService)
    }

    @Test
    fun `should be of type 'caselist'`() {
        assertThat(importer.type()).isEqualTo("decisiondefinition")
    }

    @Test
    fun `should not depend on any type`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf(CASE_DEFINITION))
    }

    @Test
    fun `should support decision definition fileName`() {
        assertThat(importer.supports("/dmn/mydecision.dmn")).isTrue()
        assertThat(importer.supports("/dmn/x/test.dmn")).isTrue()
    }

    @Test
    fun `should not support non-dmn fileName`() {
        assertThat(importer.supports("/bpmn/test.json")).isFalse()
        assertThat(importer.supports("/bpmn/x/test.dmn")).isFalse()
        assertThat(importer.supports("/dmn/test.json")).isFalse()
        assertThat(importer.supports("/dmn/test-dmn")).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val dmnContent = "<some-xml />"
        importer.import(ImportRequest(FILENAME, dmnContent.toByteArray()))

        val caseDefinitionIdCaptor = argumentCaptor<CaseDefinitionId>()
        val nameCaptor = argumentCaptor<String>()
        val contentCaptor = argumentCaptor<ByteArrayInputStream>()

        verify(operatonProcessService).deploy(
            caseDefinitionIdCaptor.capture(),
            nameCaptor.capture(),
            contentCaptor.capture()
        )

        assertThat(nameCaptor.firstValue).isEqualTo("mydecision.dmn")
        val contentValue = contentCaptor.firstValue.readAllBytes().toString(Charsets.UTF_8)
        assertThat(contentValue).isEqualTo(dmnContent)
    }

    private companion object {
        const val FILENAME = "/dmn/mydecision.dmn"
    }
}