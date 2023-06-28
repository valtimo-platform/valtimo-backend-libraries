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

package com.ritense.valtimo.formflow.autodeployment

import com.ritense.processlink.repository.ProcessLinkRepository
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import com.ritense.valtimo.formflow.BaseIntegrationTest
import com.ritense.valtimo.formflow.domain.FormFlowProcessLink
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


class ProcessLinkDeploymentApplicationReadyEventListenerIntTest @Autowired constructor(
    private val repositoryService: CamundaRepositoryService,
    private val processLinkRepository: ProcessLinkRepository
): BaseIntegrationTest() {

    @Test
    fun `should find 1 deployed process link on user task`() {
        val processDefinition = getLatestProcessDefinition()
        val processLinks =
            processLinkRepository.findByProcessDefinitionIdAndActivityId(processDefinition!!.id, "do-something")

        assertThat(processLinks, hasSize(1))
        val processLink = processLinks.first()
        assertThat(processLink, Matchers.isA(FormFlowProcessLink::class.java))
        processLink as FormFlowProcessLink
        assertThat(processLink.formFlowDefinitionId, equalTo("inkomens_loket:latest"))
    }

    private fun getLatestProcessDefinition(): CamundaProcessDefinition? {
        return repositoryService.findLatestProcessDefinition("processlink-autodeploy")
    }
}