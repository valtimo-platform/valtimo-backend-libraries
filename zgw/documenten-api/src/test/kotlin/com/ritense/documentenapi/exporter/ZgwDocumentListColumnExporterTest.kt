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

package com.ritense.documentenapi.exporter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.documentenapi.deployment.ZgwDocumentListColumn
import com.ritense.documentenapi.deployment.ZgwDocumentListColumnChangeset
import com.ritense.documentenapi.domain.ColumnDefaultSort.ASC
import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnId
import com.ritense.documentenapi.domain.DocumentenApiColumnKey
import com.ritense.documentenapi.repository.DocumentenApiColumnRepository
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ZgwDocumentListColumnExporterTest(
    @Mock private val documentenApiColumnRepository: DocumentenApiColumnRepository
) {

    private lateinit var exporter: ZgwDocumentListColumnExporter

    @BeforeEach
    fun before() {
        exporter = ZgwDocumentListColumnExporter(documentenApiColumnRepository, jacksonObjectMapper())
    }

    @Test
    fun `should not export changeset when no documentlist columns are configured`() {
        val export = exporter.export(DocumentDefinitionExportRequest("test", 1L))

        assertThat(export.exportFiles).isEmpty()
    }

    @Test
    fun `should export changeset for documentlist columns`() {
        val name = "test"

        whenever(documentenApiColumnRepository.findAllByIdCaseDefinitionNameOrderByOrder(name)).thenReturn(
            listOf(
                DocumentenApiColumn(DocumentenApiColumnId(name, DocumentenApiColumnKey.AUTEUR), 0, ASC),
                DocumentenApiColumn(DocumentenApiColumnId(name, DocumentenApiColumnKey.BESTANDSNAAM))
            )
        )

        val export = exporter.export(DocumentDefinitionExportRequest(name, 1L))

        assertThat(export.exportFiles).hasSize(1)
        val file = export.exportFiles.first()
        assertThat(file.path).isEqualTo("config/case/zgw-document-list-columns/test.zgw-document-list-column.json")
        val value = jacksonObjectMapper().readValue<ZgwDocumentListColumnChangeset>(file.content)
        assertThat(value.changesetId).matches("""test\.zgw-document-list-column\.\d+""")
        assertThat(value.caseDefinitions).hasSize(1)
        val columnCollection = value.caseDefinitions.first()
        assertThat(columnCollection.key).isEqualTo(name)
        assertThat(columnCollection.columns).containsExactly(
            ZgwDocumentListColumn(DocumentenApiColumnKey.AUTEUR, ASC),
            ZgwDocumentListColumn(DocumentenApiColumnKey.BESTANDSNAAM, null),
        )
    }

}