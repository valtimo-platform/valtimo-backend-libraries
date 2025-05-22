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

import com.ritense.document.service.SearchConfigurationDeploymentService
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
class SearchFieldImporterTest(
    @Mock private val searchConfigurationDeploymentService: SearchConfigurationDeploymentService
) {
    private lateinit var importer: SearchFieldImporter

    @BeforeEach
    fun before() {
        importer = SearchFieldImporter(searchConfigurationDeploymentService)
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
        assertThat(importer.supports("config/search/not/person.json")).isFalse()
        assertThat(importer.supports("config/search/person.xml")).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = this::class.java.getResource("/$FILENAME")!!.readText(Charsets.UTF_8)

        importer.import(ImportRequest(FILENAME, jsonContent.toByteArray()))

        val jsonCaptor = argumentCaptor<String>()
        verify(searchConfigurationDeploymentService).deploy(jsonCaptor.capture(), jsonCaptor.capture())

        assertThat(jsonCaptor.firstValue).isEqualTo("person")
        assertThat(jsonCaptor.secondValue).isEqualTo(jsonContent)
    }

    private companion object {
        const val FILENAME = "config/search/person.json"
    }
}