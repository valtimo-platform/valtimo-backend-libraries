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

package com.ritense.processlink.importer

import com.ritense.authorization.AuthorizationContext
import com.ritense.importer.ImportRequest
import com.ritense.importer.ValtimoImportTypes.Companion.PROCESS_DEFINITION
import com.ritense.processlink.BaseIntegrationTest
import com.ritense.processlink.domain.CustomProcessLink
import com.ritense.processlink.repository.ProcessLinkRepository
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
@ExtendWith(MockitoExtension::class)
class ProcessLinkImporterIntTest @Autowired constructor(
    private val processLinkImporter: ProcessLinkImporter,
    private val processLinkRepository: ProcessLinkRepository,
    private val repositoryService: CamundaRepositoryService
) : BaseIntegrationTest() {

    @Test
    fun `should dependsOn types from mappers`() {
        assertThat(processLinkImporter.dependsOn()).isEqualTo(setOf(PROCESS_DEFINITION, "test"))
    }

    @Test
    fun `should deploy processLinks`() {
        processLinkImporter.import(ImportRequest(
            "test-importer-process.processlink.json",
            JSON.toByteArray(Charsets.UTF_8)
        ))

        val processDefinition = getLatestProcessDefinition()
        val processLinks =
            processLinkRepository.findByProcessDefinitionIdAndActivityId(processDefinition.id, "my-service-task")

        val processLink = requireNotNull(processLinks.single() as? CustomProcessLink)
        assertThat(processLink.someValue).isEqualTo("importer test")
    }

    private fun getLatestProcessDefinition(): CamundaProcessDefinition {
        return AuthorizationContext.runWithoutAuthorization {
            repositoryService.findLatestProcessDefinition("test-importer-process")!!
        }
    }

    private companion object {
        const val JSON = """
            [
                {
                    "activityId": "my-service-task",
                    "activityType": "bpmn:ServiceTask:start",
                    "processLinkType": "test",
                    "someValue": "importer test"
                }
            ]
        """
    }
}