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

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.exporter.request.CaseDefinitionExportRequest
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils

@Transactional(readOnly = true)
class CaseDefinitionExporterIntTest @Autowired constructor(
    private val resourceLoader: ResourceLoader,
    private val caseDefinitionExporter: CaseDefinitionExporter,
    private val caseDefinitionRepository: CaseDefinitionRepository
) : BaseIntegrationTest() {

    @Test
    fun `should export tabs for case definition`(): Unit = runWithoutAuthorization {
        val caseDefinitionKey = "some-case-type"
        val caseDefinitionVersionTag = "1.2.3"

        caseDefinitionRepository.save(
            caseDefinition(
                CaseDefinitionId(
                    caseDefinitionKey,
                    caseDefinitionVersionTag
                ),
                name = "Some case type",
                canHaveAssignee = true,
                autoAssignTasks = true,
            )
        )

        val request = CaseDefinitionExportRequest(CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag))
        val exportResult = caseDefinitionExporter.export(request)

        val path = PATH.format(caseDefinitionKey, caseDefinitionKey)
        val caseTabsExport = exportResult.exportFiles.singleOrNull {
            it.path == path
        }
        requireNotNull(caseTabsExport)
        val exportJson = caseTabsExport.content.toString(Charsets.UTF_8)
        val expectedJson = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/case/$caseDefinitionKey/1-2-3/case/definition/$caseDefinitionKey.json")
            .inputStream
            .use { inputStream ->
                StreamUtils.copyToString(inputStream, Charsets.UTF_8)
            }
        JSONAssert.assertEquals(
            expectedJson,
            exportJson,
            JSONCompareMode.LENIENT
        )
    }

    companion object {
        private const val PATH = "config/case/%s/1-2-3/case/definition/%s.json"
    }
}