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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.authorization.AuthorizationContext
import com.ritense.case.BaseIntegrationTest
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils

@Transactional(readOnly = true)
class CaseWidgetTabExporterIntTest @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val resourceLoader: ResourceLoader,
    private val exporter: CaseWidgetTabExporter
) : BaseIntegrationTest() {
    @Test
    fun `should export list columns for case definition`(): Unit = AuthorizationContext.runWithoutAuthorization {
        val caseDefinitionName = "some-other-case-type"

        val request = DocumentDefinitionExportRequest(caseDefinitionName, 1)
        val exportResult = exporter.export(request)

        val path = PATH.format(caseDefinitionName)
        val caseWidgetTabsExport = exportResult.exportFiles.singleOrNull {
            it.path == path
        }
        requireNotNull(caseWidgetTabsExport)
        val exportJson = objectMapper.readTree(caseWidgetTabsExport.content)

        //Check if the changesetId ends with a timestamp
        val changesetIdField = "changesetId"
        val changesetRegex = """(some-other-case-type\.case-widget-tab)\.\d+""".toRegex()
        val matchResult = changesetRegex.matchEntire(exportJson.get(changesetIdField).textValue())
        assertThat(matchResult).isNotNull

        //Remove the timestamp from the changesetId, so we can compare it as usual
        (exportJson as ObjectNode).set<TextNode>(changesetIdField, TextNode(matchResult!!.groupValues[1]))
        val expectedJson = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/case-tabs/$caseDefinitionName.case-widget-tab.json")
            .inputStream
            .use { inputStream ->
                StreamUtils.copyToString(inputStream, Charsets.UTF_8)
            }
        JSONAssert.assertEquals(
            expectedJson,
            objectMapper.writeValueAsString(exportJson),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    companion object {
        private const val PATH = "config/case-widget-tab/%s.case-widget-tab.json"
    }
}