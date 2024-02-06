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

package com.ritense.processdocument.exporter

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import com.ritense.exporter.request.ProcessDefinitionExportRequest
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
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
class ProcessDocumentLinkExporterIntTest @Autowired constructor(
    private val resourceLoader: ResourceLoader,
    private val camundaRepositoryService: CamundaRepositoryService,
    private val processDocumentLinkExporter: ProcessDocumentLinkExporter
) : BaseIntegrationTest() {

    @Test
    fun `should export process document links`(): Unit = runWithoutAuthorization {
        val documentDefinitionName = "house"
        val result = processDocumentLinkExporter.export(DocumentDefinitionExportRequest(documentDefinitionName, 1))

        val exportFile = result.exportFiles.single {
            it.path == PATH.format(documentDefinitionName)
        }

        val exportJson = exportFile.content.toString(Charsets.UTF_8)
        val expectedJson = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:${PATH.format(documentDefinitionName)}")
            .inputStream
            .use { inputStream ->
                StreamUtils.copyToString(inputStream, Charsets.UTF_8)
            }
        JSONAssert.assertEquals(
            expectedJson,
            exportJson,
            JSONCompareMode.NON_EXTENSIBLE
        )

        val processDefinitionId = camundaRepositoryService.findLatestProcessDefinition("loan-process-demo")!!.id
        assertThat(result.relatedRequests).contains(
            ProcessDefinitionExportRequest(processDefinitionId)
        )
    }

    companion object {
        private const val PATH = "config/process-document-link/%s.json";
    }
}