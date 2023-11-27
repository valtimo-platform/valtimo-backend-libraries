package com.ritense.valtimo.export

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.export.request.DecisionDefinitionExportRequest
import com.ritense.valtimo.BaseIntegrationTest
import java.io.ByteArrayInputStream
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.model.dmn.Dmn
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

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
            it.path == "config/bpmn/$decisionDefinitionKey.dmn"
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