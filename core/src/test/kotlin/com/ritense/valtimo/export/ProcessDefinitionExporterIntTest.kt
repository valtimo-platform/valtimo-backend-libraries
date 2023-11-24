package com.ritense.valtimo.export

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.export.request.DecisionDefinitionExportRequest
import com.ritense.export.request.ProcessDefinitionExportRequest
import com.ritense.valtimo.BaseIntegrationTest
import java.io.ByteArrayInputStream
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class ProcessDefinitionExporterIntTest @Autowired constructor(
    private val processDefinitionExporter: ProcessDefinitionExporter
) : BaseIntegrationTest() {

    @Test
    fun `should export process definition`(): Unit = runWithoutAuthorization {
        val result = processDefinitionExporter.export(ProcessDefinitionExportRequest("dmn-sample"))

        assertThat(result.exportFiles).isNotEmpty()

        val bpmnExportFile = result.exportFiles.singleOrNull {
            it.path == "config/bpmn/dmn-sample.bpmn"
        }

        requireNotNull(bpmnExportFile)
        val bpmnModelInstance = ByteArrayInputStream(bpmnExportFile.content).use {
            Bpmn.readModelFromStream(it)
        }
        assertThat(bpmnModelInstance).isNotNull

        val decisionExportRequest = result.nestedRequests
            .singleOrNull {
                it is DecisionDefinitionExportRequest && it.key == "dmn-sample"
            }
        assertThat(decisionExportRequest).isNotNull
    }
}