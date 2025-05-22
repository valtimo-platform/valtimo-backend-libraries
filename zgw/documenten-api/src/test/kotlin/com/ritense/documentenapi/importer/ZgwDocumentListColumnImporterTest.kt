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

package com.ritense.documentenapi.importer

import com.ritense.documentenapi.deployment.ZgwDocumentListColumnDeploymentService
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class ZgwDocumentListColumnImporterTest(
    @Mock private val deployer: ZgwDocumentListColumnDeploymentService,
    @Mock private val changelogDeployer: ChangelogDeployer,
) {
    private lateinit var importer: ZgwDocumentListColumnImporter

    @BeforeEach
    fun before() {
        importer = ZgwDocumentListColumnImporter(deployer, changelogDeployer)
    }

    @Test
    fun `should be of type 'ZGW_DOCUMENT_LIST_COLUMN'`() {
        Assertions.assertThat(importer.type()).isEqualTo(ValtimoImportTypes.ZGW_DOCUMENT_LIST_COLUMN)
    }

    @Test
    fun `should depend on 'documentdefinition'`() {
        Assertions.assertThat(importer.dependsOn())
            .isEqualTo(setOf(ValtimoImportTypes.DOCUMENT_DEFINITION))
    }

    @Test
    fun `should support zgwDocumentListColumn fileName`() {
        Assertions.assertThat(importer.supports(FILENAME)).isTrue()
    }

    @Test
    fun `should not support non-zgwDocumentListColumn fileName`() {
        Assertions.assertThat(
            importer.supports("config/case/t.zgw-document-list-column.json")
        ).isFalse()
        Assertions.assertThat(
            importer.supports("config/case/zgw-document-list-columns/my-file.json")
        ).isFalse()
    }

    @Test
    fun `should call deploy method for import with correct parameters`() {
        val jsonContent = "{}"

        importer.import(ImportRequest(FILENAME, jsonContent.toByteArray()))

        verify(changelogDeployer).deploy(deployer, FILENAME, jsonContent)
    }

    private companion object {
        const val FILENAME = "config/case/zgw-document-list-columns/my-file.zgw-document-list-column.json"
    }
}