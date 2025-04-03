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

import com.ritense.document.deployment.CaseTagDeployer
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class CaseTagImporterTest(
    @Mock private val caseTagDeployer: CaseTagDeployer,
    @Mock private val changelogDeployer: ChangelogDeployer
) {
    private lateinit var importer: CaseTagImporter

    @BeforeEach
    fun before() {
        importer = CaseTagImporter(caseTagDeployer, changelogDeployer)
    }

    @Test
    fun `should be of type 'casetag'`() {
        assertThat(importer.type()).isEqualTo("casetag")
    }

    @Test
    fun `should depend on 'documentdefinition'`() {
        assertThat(importer.dependsOn()).isEqualTo(setOf(DOCUMENT_DEFINITION))
    }

    @Test
    fun `should support caseTag fileName`() {
        assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-case-tag fileName`() {
        assertThat(importer.supports("config/case-tag/x/test.json")).isFalse()
        assertThat(importer.supports("config/case-tag/test-json")).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = "{}"

        importer.import(ImportRequest(FILENAME, jsonContent.toByteArray()))

        verify(changelogDeployer).deploy(caseTagDeployer, FILENAME, jsonContent)
    }

    private companion object {
        const val FILENAME = "config/case-tag/my-doc-def.case-tag.json"
    }
}