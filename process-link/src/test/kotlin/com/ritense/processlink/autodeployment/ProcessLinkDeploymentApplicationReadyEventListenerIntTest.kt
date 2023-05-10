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

package com.ritense.processlink.autodeployment

import com.ritense.processlink.BaseIntegrationTest
import com.ritense.processlink.domain.CustomProcessLink
import com.ritense.processlink.repository.ProcessLinkRepository
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


class ProcessLinkDeploymentApplicationReadyEventListenerIntTest @Autowired constructor(
    private val repositoryService: RepositoryService,
    private val processLinkRepository: ProcessLinkRepository
): BaseIntegrationTest() {

    @Test
    fun `should find 1 deployed process link on service task`() {
        val processDefinition = getLatestProcessDefinition()
        val processLinks =
            processLinkRepository.findByProcessDefinitionIdAndActivityId(processDefinition.id, "my-service-task")

        assertThat(processLinks, hasSize(1))
        val processLink = processLinks.first()
        assertThat(processLink, Matchers.isA(CustomProcessLink::class.java))
        processLink as CustomProcessLink
        assertThat(processLink.someValue, Matchers.equalTo("test"))
    }

    private fun getLatestProcessDefinition(): ProcessDefinition {
        return repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("auto-deploy-process-link")
            .latestVersion()
            .singleResult()
    }
}