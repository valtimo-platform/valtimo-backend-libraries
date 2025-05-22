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

import com.ritense.exporter.ExportFile
import com.ritense.exporter.ExportResult
import com.ritense.exporter.Exporter
import com.ritense.exporter.request.DecisionDefinitionExportRequest
import org.apache.commons.io.IOUtils
import org.camunda.bpm.engine.RepositoryService

class DecisionDefinitionExporter(
    private val repositoryService: RepositoryService
) : Exporter<DecisionDefinitionExportRequest> {
    override fun supports(): Class<DecisionDefinitionExportRequest> = DecisionDefinitionExportRequest::class.java

    override fun export(request: DecisionDefinitionExportRequest): ExportResult {
        val decisionDefinition = repositoryService.getDecisionDefinition(request.decisionDefinitionId)

        val exportFile = repositoryService.getDecisionModel(decisionDefinition.id).use {inputStream ->
            ExportFile(
                "dmn/${decisionDefinition.key}.dmn",
                IOUtils.toByteArray(inputStream)
            )
        }
        return ExportResult(exportFile)
    }
}