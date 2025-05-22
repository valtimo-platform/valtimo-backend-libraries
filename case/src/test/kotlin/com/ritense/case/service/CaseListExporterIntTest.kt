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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.authorization.AuthorizationContext
import com.ritense.case.BaseIntegrationTest
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils

@Transactional(readOnly = true)
class CaseListExporterIntTest @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val resourceLoader: ResourceLoader,
    private val caseListExporter: CaseListExporter
) : BaseIntegrationTest() {
    @Test
    fun `should export list columns for case definition`(): Unit = AuthorizationContext.runWithoutAuthorization {
        val caseDefinitionName = "house"

        val request = DocumentDefinitionExportRequest(caseDefinitionName, 1)
        val exportFiles = caseListExporter.export(request).exportFiles

        val path = PATH.format(caseDefinitionName)
        val caseTabsExport = exportFiles.singleOrNull {
            it.path == path
        }

        val jsonTree = objectMapper.readTree(requireNotNull(caseTabsExport).content)
        //The order is empty at the resource, but serialized anyway. Remove it to fix the comparison
        (jsonTree.at("/1") as ObjectNode).remove("order")

        val expectedJson = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/case/list/$caseDefinitionName.json")
            .inputStream
            .use { inputStream ->
                StreamUtils.copyToString(inputStream, Charsets.UTF_8)
            }
        JSONAssert.assertEquals(
            expectedJson,
            objectMapper.writeValueAsString(jsonTree),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    companion object {
        private const val PATH = "config/case/list/%s.json"
    }
}