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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
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
class CaseDefinitionImporterTest(
    @Mock private val objectMapper: ObjectMapper = ObjectMapper(),
    @Mock private val caseDefinitionRepository: CaseDefinitionRepository
) {
    private lateinit var importer: CaseDefinitionImporter

    @BeforeEach
    fun before() {
        importer = CaseDefinitionImporter(objectMapper, caseDefinitionRepository)
    }

    @Test
    fun `should be of type 'casesettings'`() {
        assertThat(importer.type()).isEqualTo("casedefinition")
    }

    @Test
    fun `should not depend on any type`() {
        assertThat(importer.dependsOn()).isEqualTo(emptySet<String>())
    }

    @Test
    fun `should support casesettings fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-caselist fileName`() {
        assertThat(importer.supports("config/case/definition/x/test.json")).isFalse()
        assertThat(importer.supports("config/case/definition/test-json")).isFalse()
    }

    private companion object {
        const val FILENAME = "/case/definition/my-case-list.json"
    }
}