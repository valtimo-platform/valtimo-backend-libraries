package com.ritense.processlink.export

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.export.request.ProcessDefinitionExportRequest
import com.ritense.processlink.BaseIntegrationTest
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ProcessLinkExporterIntTest @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val camundaRepositoryService: CamundaRepositoryService,
    private val processLinkExporter: ProcessLinkExporter
) : BaseIntegrationTest() {

    @Test
    fun `should export process links`(): Unit = runWithoutAuthorization {
        val processDefinitionKey = "auto-deploy-process-link"
        val processDefinitionId = getProcessDefinitionId(processDefinitionKey)

        val result = processLinkExporter.export(ProcessDefinitionExportRequest(processDefinitionId))

        assertThat(result.exportFiles).isNotEmpty()

        val exportFile = result.exportFiles.single {
            it.path == "config/auto-deploy-process-link.processlink.json"
        }

        val createRequestDtos: List<ProcessLinkExportResponseDto> = objectMapper.readValue(exportFile.content)
        assertThat(createRequestDtos).isNotEmpty

        assertThat(result.relatedRequests).contains(CustomProcessLinkNestedExportRequest())
    }

    fun getProcessDefinitionId(processDefinitionKey: String): String {
        return requireNotNull(
            camundaRepositoryService.findProcessDefinition(
                CamundaProcessDefinitionSpecificationHelper.byKey(processDefinitionKey)
                    .and(CamundaProcessDefinitionSpecificationHelper.byLatestVersion())
            )
        ).id
    }
}