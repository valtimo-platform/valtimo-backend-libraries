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

package com.ritense.documentenapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.authorization.AuthorizationContext
import com.ritense.documentenapi.BaseIntegrationTest
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils

@Transactional(readOnly = true)
class ZgwDocumentTrefwoordExporterIntTest @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val resourceLoader: ResourceLoader,
    private val zgwDocumentTrefwoordExporter: ZgwDocumentTrefwoordExporter
) : BaseIntegrationTest() {

    private val caseDefinitionId = CaseDefinitionId("profile", "1.0.0")

    @Test
    fun `should export zgw document trefwoorden for case definition`(): Unit = AuthorizationContext.runWithoutAuthorization {
        val caseDefinitionName = "profile"

        val request = DocumentDefinitionExportRequest(caseDefinitionName, caseDefinitionId)
        val exportFiles = zgwDocumentTrefwoordExporter.export(request).exportFiles

        val path = PATH.format(caseDefinitionName)
        val trefwoordenExport = exportFiles.singleOrNull {
            it.path == path
        }

        val jsonTree = objectMapper.readTree(requireNotNull(trefwoordenExport).content)

        val expectedJson = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/case/profile/1-0-0/zgw/trefwoord/$caseDefinitionName.zgw-document-trefwoord.json")
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
        private const val PATH = "config/case/profile/1-0-0/zgw/trefwoord/%s.zgw-document-trefwoord.json"

    }
}