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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.service.SearchFieldService
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.springframework.core.io.ResourceLoader

@ExtendWith(MockitoExtension::class)
class SearchFieldImporterTest(
    @Mock private val resourceLoader: ResourceLoader,
    @Mock private val searchFieldService: SearchFieldService,
    @Mock private val objectMapper: ObjectMapper,
) {
    private lateinit var importer: SearchFieldImporter

    @BeforeEach
    fun before() {
        importer = spy(SearchFieldImporter(resourceLoader, searchFieldService, objectMapper))
    }

    @Test
    fun `should be of type 'search'`() {
        assertThat(importer.type()).isEqualTo("search")
    }

    @Test
    fun `should depend on set of 'documentdefinition'`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf(DOCUMENT_DEFINITION))
    }

    @Test
    fun `should support search fields fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support invalid document definition fileName`() {
        assertThat(importer.supports("/search/not/person.json")).isFalse()
        assertThat(importer.supports("/search/person.xml")).isFalse()
    }

    private companion object {
        const val FILENAME = "/search/person.json"
    }
}