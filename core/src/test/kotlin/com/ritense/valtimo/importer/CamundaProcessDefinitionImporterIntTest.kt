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

package com.ritense.valtimo.importer

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.importer.ImportRequest
import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.exception.FileExtensionNotSupportedException
import com.ritense.valtimo.service.CamundaProcessService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource

class CamundaProcessDefinitionImporterIntTest @Autowired constructor(
    private val camundaProcessDefinitionImporter: CamundaProcessDefinitionImporter,
    private val camundaProcessService: CamundaProcessService,
    @Value("classpath:examples/bpmn/shouldDeploy.bpmn")
    private val processDefinition: Resource,
    @Value("classpath:examples/bpmn/shouldDeploy.bpmn")
    private val processDefinitionAsXml: Resource
) : BaseIntegrationTest() {
    @Test
    fun `should import process definition with bpmn extension`() {
        val validPath = "bpmn/shouldDeploy.bpmn"
        val request = processDefinition.inputStream.use {
            ImportRequest(validPath, it.readAllBytes())
        }

        runWithoutAuthorization {
            camundaProcessDefinitionImporter.import(request)
        }
        val storedDefinition = runWithoutAuthorization {
            camundaProcessService.getProcessDefinition("deployedProcess")
        }
        assertThat(storedDefinition).isNotNull
    }

    @Test
    fun `should not import process definition with xml extension`() {
        val validPath = "bpmn/shouldDeploy.xml"
        val request = processDefinition.inputStream.use {
            ImportRequest(validPath, it.readAllBytes())
        }

        assertThrows(FileExtensionNotSupportedException::class.java) {
            runWithoutAuthorization {
                camundaProcessDefinitionImporter.import(request)
            }
        }
    }

}