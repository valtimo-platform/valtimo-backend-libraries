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

package com.ritense.search.exporter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.domain.SearchFieldMatchType
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.service.SearchFieldV2Service
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertNotNull

class SearchFieldExporterTest {

    private lateinit var testExporter: SearchFieldExporter

    private lateinit var objectMapper: ObjectMapper
    private lateinit var searchFieldService: SearchFieldV2Service

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().registerKotlinModule()
        searchFieldService = mock()

        testExporter = TestSearchFieldExporter(
            objectMapper,
            searchFieldService,
        )
    }

    @Test
    fun `should support DocumentDefinitionExportRequest`() {
        assertEquals(DocumentDefinitionExportRequest::class.java, testExporter.supports())
    }

    @Test
    fun `should not export when no searchfields are configured`() {
        val request = DocumentDefinitionExportRequest(
            name = "empty-document-definition-name",
            version = 1L
        )

        val result = testExporter.export(request)

        assertEquals(0, result.exportFiles.size)
    }

    @Test
    fun `should return changeset with correct details`() {
        val request = DocumentDefinitionExportRequest(
            name = "my-document-definition-name",
            version = 1L
        )
        whenever(searchFieldService.findAllByOwnerTypeAndOwnerId(testExporter.ownerTypeKey(), request.name)).thenReturn(
            listOf(
                SearchFieldV2(
                    ownerId = request.name,
                    ownerType = testExporter.ownerTypeKey(),
                    key = "firstname",
                    title = "Firstname",
                    path = "doc:firstname",
                    order = 0,
                    dataType = DataType.TEXT,
                    fieldType = FieldType.SINGLE,
                    matchType = SearchFieldMatchType.LIKE,
                ),
                SearchFieldV2(
                    ownerId = request.name,
                    ownerType = testExporter.ownerTypeKey(),
                    key = "lastname",
                    title = "Lastname",
                    path = "doc:lastname",
                    order = 1,
                    dataType = DataType.TEXT,
                    fieldType = FieldType.SINGLE,
                    matchType = SearchFieldMatchType.LIKE,
                )
            )
        )

        val result = testExporter.export(request)

        val path = testExporter.getPath(request.name)
        val ownerTypeKey = testExporter.ownerTypeKey()
        val caseTaskListExport = result.exportFiles.singleOrNull {
            it.path == path
        }
        requireNotNull(caseTaskListExport)
        val exportJson = objectMapper.readTree(caseTaskListExport.content)

        //Check if the changesetId ends with a timestamp
        val changesetIdField = "changesetId"
        val changesetRegex = """(${request.name}\.$ownerTypeKey)\.\d+""".toRegex()
        val matchResult = changesetRegex.matchEntire(exportJson.get(changesetIdField).textValue())
        assertNotNull(matchResult)

        //Remove the timestamp from the changesetId, so we can compare it as usual
        (exportJson as ObjectNode).set<TextNode>(changesetIdField, TextNode(matchResult.groupValues[1]))
        JSONAssert.assertEquals(
            """{"changesetId":"my-document-definition-name.some-owner-type","collection":[{"ownerId":"my-document-definition-name","searchFields":[{"key":"firstname","title":"Firstname","path":"doc:firstname","dataType":"text","fieldType":"single","matchType":"like"},{"key":"lastname","title":"Lastname","path":"doc:lastname","dataType":"text","fieldType":"single","matchType":"like"}]}]}""",
            objectMapper.writeValueAsString(exportJson),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    private class TestSearchFieldExporter(
        objectMapper: ObjectMapper,
        searchFieldService: SearchFieldV2Service,
    ) : SearchFieldExporter(objectMapper, searchFieldService) {
        override fun getPath(documentDefinitionName: String): String = "some/$documentDefinitionName/path"

        override fun ownerTypeKey(): String = "some-owner-type"
    }
}