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

package com.ritense.case_.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.case_.rest.dto.CaseWidgetTabWidgetDto
import com.ritense.case_.widget.CaseWidgetMapper
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_TAB
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.FORM
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CaseWidgetTabImporterTest(
    @Mock private val objectMapper: ObjectMapper,
    @Mock private val validator: Validator,
    @Mock private val caseWidgetTabRepository: CaseWidgetTabRepository,
    @Mock private val caseWidgetMappers: List<CaseWidgetMapper<CaseWidgetTabWidget, CaseWidgetTabWidgetDto>>,
) {
    private lateinit var importer: CaseWidgetTabImporter

    @BeforeEach
    fun before() {
        importer = CaseWidgetTabImporter(objectMapper, validator, caseWidgetTabRepository, caseWidgetMappers)
    }

    @Test
    fun `should be of type 'casetab'`() {
        assertThat(importer.type()).isEqualTo("casewidgettab")
    }

    @Test
    fun `should depend on 'documentdefinition' and 'form' type`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf(DOCUMENT_DEFINITION, CASE_TAB, FORM))
    }

    @Test
    fun `should support caseTab fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-caseTab fileName`() {
        assertThat(importer.supports("/case/widget-tab/x/test.json")).isFalse()
        assertThat(importer.supports("/case/widget-tab/test-json")).isFalse()
    }

    private companion object {
        const val FILENAME = "/case/widget-tab/my-doc-def.case-widget-tab.json"
    }
}