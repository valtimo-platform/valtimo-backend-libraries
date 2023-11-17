package com.ritense.processlink.service

import com.ritense.authorization.AuthorizationContext
import com.ritense.processlink.BaseIntegrationTest
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ProcessLinkServiceIntTest @Autowired constructor(
    private val camundaRepositoryService: CamundaRepositoryService,
    private val processLinkService: ProcessLinkService
) : BaseIntegrationTest() {


    @Test
    fun `should get all process links by proces definition- and activity id`() {
        AuthorizationContext.runWithoutAuthorization {
            val processDefinition = camundaRepositoryService.findLatestProcessDefinition("auto-deploy-process-link")!!
            val processLinks = processLinkService.getProcessLinks(processDefinition.id, "my-service-task")
            assertThat(processLinks.all { it.processDefinitionId == processDefinition.id && it.activityId == "my-service-task"}).isTrue()
        }
    }

    @Test
    fun `should get all process links by proces definition id`() {
        AuthorizationContext.runWithoutAuthorization {
            val processDefinition = camundaRepositoryService.findLatestProcessDefinition("auto-deploy-process-link")!!
            val processLinks = processLinkService.getProcessLinks(processDefinition.id, null)
            assertThat(processLinks).hasSizeGreaterThanOrEqualTo(2)
            assertThat(processLinks.all { it.processDefinitionId == processDefinition.id }).isTrue()
            assertThat(processLinks.singleOrNull { it.activityId == "my-service-task" }).isNotNull
            assertThat(processLinks.singleOrNull { it.activityId == "start-event" }).isNotNull
        }
    }
}