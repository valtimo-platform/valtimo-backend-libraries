package com.ritense.valtimo.export

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.export.request.DecisionDefinitionExportRequest
import com.ritense.valtimo.BaseIntegrationTest
import java.io.ByteArrayInputStream
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.model.dmn.Dmn
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class DecisionDefinitionExporterIntTest @Autowired constructor(
    private val decisionDefinitionExporter: DecisionDefinitionExporter
) : BaseIntegrationTest() {

    @Test
    fun `should export process definition`(): Unit = runWithoutAuthorization {
        val result = decisionDefinitionExporter.export(DecisionDefinitionExportRequest("dmn-sample"))

        assertThat(result.exportFiles).isNotEmpty()

        val dmnExportFile = result.exportFiles.singleOrNull {
            it.path == "config/bpmn/dmn-sample.dmn"
        }

        requireNotNull(dmnExportFile)
        val dmnModelInstance = ByteArrayInputStream(dmnExportFile.content).use {
            Dmn.readModelFromStream(it)
        }
        assertThat(dmnModelInstance).isNotNull
    }
}