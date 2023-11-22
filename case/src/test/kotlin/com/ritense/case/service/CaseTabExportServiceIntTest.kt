/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.util.StreamUtils

class CaseTabExportServiceIntTest @Autowired constructor(
    private val resourceLoader: ResourceLoader,
    private val caseTabExportService: CaseTabExportService
) : BaseIntegrationTest() {

    @Test
    fun `should export tabs for case definition`(): Unit = runWithoutAuthorization {
        val caseDefinitionName = "some-case-type"

        val exportFiles = caseTabExportService.export(JsonSchemaDocumentDefinitionId.newId(caseDefinitionName))

        val path = CaseTabExportService.PATH.format(caseDefinitionName)
        val caseTabsExport = exportFiles.singleOrNull {
            it.path == path
        }
        Assertions.assertThat(caseTabsExport).isNotNull
        requireNotNull(caseTabsExport)
        val content = caseTabsExport.content.toString(Charsets.UTF_8)
        val expectedString = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:config/case-tabs/$caseDefinitionName.case-tabs.json")
            .inputStream
            .use { inputStream ->
                StreamUtils.copyToString(inputStream, Charsets.UTF_8)
            }
        JSONAssert.assertEquals(
            expectedString,
            content,
            JSONCompareMode.NON_EXTENSIBLE
        )
    }
}