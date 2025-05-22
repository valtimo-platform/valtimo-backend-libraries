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

package com.ritense.search.importer

import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes
import com.ritense.search.deployment.SearchFieldDeployer
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class SearchFieldImporterTest(
    @Mock private val searchFieldDeployer: SearchFieldDeployer,
    @Mock private val changelogDeployer: ChangelogDeployer,
) {
    private lateinit var importer: TestSearchFieldImporter

    @BeforeEach
    fun before() {
        importer = TestSearchFieldImporter(searchFieldDeployer, changelogDeployer)
    }

    @Test
    fun `should be of type 'testSearchField'`() {
        Assertions.assertThat(importer.type()).isEqualTo("testSearchField")
    }

    @Test
    fun `should depend on 'documentdefinition' and 'form' type`() {
        Assertions.assertThat(importer.dependsOn())
            .isEqualTo(setOf(ValtimoImportTypes.DOCUMENT_DEFINITION))
    }

    @Test
    fun `should support searchField fileName`() {
        Assertions.assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-searchField fileName`() {
        Assertions.assertThat(importer.supports("config/test-search-field/x/test.test-search-field.json")).isFalse()
        Assertions.assertThat(importer.supports("config/test-search-field/test-test-search-field.json")).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = "{}"

        importer.import(ImportRequest(FILENAME, jsonContent.toByteArray()))

        verify(changelogDeployer).deploy(searchFieldDeployer, FILENAME, jsonContent)
    }

    private class TestSearchFieldImporter(
        searchFieldDeployer: SearchFieldDeployer,
        changelogDeployer: ChangelogDeployer,
    ) : SearchFieldImporter(searchFieldDeployer, changelogDeployer) {
        override fun ownerTypeKey(): String = "some-owner-type"
        override fun getPathRegex(): Regex = """config/test-search-field/([^/]+)\.test-search-field.json""".toRegex()
        override fun type(): String = "testSearchField"
    }

    private companion object {
        const val FILENAME = "config/test-search-field/my-doc-def.test-search-field.json"
    }
}
