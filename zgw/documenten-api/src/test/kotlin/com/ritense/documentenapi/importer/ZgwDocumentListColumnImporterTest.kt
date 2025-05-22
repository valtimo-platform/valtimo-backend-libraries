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

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ZgwDocumentListColumnImporterTest(
    @Mock private val objectMapper: ObjectMapper,
    @Mock private val documentenApiColumnRepository: DocumentenApiColumnRepository,
    @Mock private val documentenApiService: DocumentenApiService,
) {
    private lateinit var importer: ZgwDocumentListColumnImporter

    @BeforeEach
    fun before() {
        importer = ZgwDocumentListColumnImporter(objectMapper, documentenApiColumnRepository, documentenApiService)
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
            importer.supports("config/case/zgw-document-list-column/my-file.json")
        ).isFalse()
    }

    private companion object {
        const val FILENAME = "/zgw/document-list-column/my-file.zgw-document-list-column.json"
    }
}