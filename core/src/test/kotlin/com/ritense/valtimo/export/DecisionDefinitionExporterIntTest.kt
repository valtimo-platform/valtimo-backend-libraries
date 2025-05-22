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

package com.ritense.valtimo.export

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.exporter.request.DecisionDefinitionExportRequest
import com.ritense.valtimo.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.model.dmn.Dmn
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream

@Transactional(readOnly = true)
class DecisionDefinitionExporterIntTest @Autowired constructor(
    private val repositoryService: RepositoryService,
    private val decisionDefinitionExporter: DecisionDefinitionExporter
) : BaseIntegrationTest() {

    @Test
    fun `should export process definition`(): Unit = runWithoutAuthorization {
        val decisionDefinitionKey = "dmn-sample"
        val decisionDefinitionId = getDecisionDefinitionId(decisionDefinitionKey)
        val result = decisionDefinitionExporter.export(DecisionDefinitionExportRequest(decisionDefinitionId))

        assertThat(result.exportFiles).isNotEmpty()

        val dmnExportFile = result.exportFiles.singleOrNull {
            it.path == "dmn/$decisionDefinitionKey.dmn"
        }

        requireNotNull(dmnExportFile)
        val dmnModelInstance = ByteArrayInputStream(dmnExportFile.content).use {
            Dmn.readModelFromStream(it)
        }
        assertThat(dmnModelInstance).isNotNull
    }

    fun getDecisionDefinitionId(decisionDefinitionKey:String): String {
        return repositoryService.createDecisionDefinitionQuery()
            .decisionDefinitionKey(decisionDefinitionKey)
            .latestVersion()
            .singleResult()
            .id
    }
}