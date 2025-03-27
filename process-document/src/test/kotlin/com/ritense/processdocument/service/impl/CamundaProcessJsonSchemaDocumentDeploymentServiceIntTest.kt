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

package com.ritense.processdocument.service.impl

import com.ritense.authorization.AuthorizationContext
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionId
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentDeploymentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Transactional
internal class CamundaProcessJsonSchemaDocumentDeploymentServiceIntTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var processDocumentDeploymentService: ProcessDocumentDeploymentService
    @Autowired
    private lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Test
    fun `should deploy process document link`() {
        val linkJson = readFileAsString("/config/more-process-document-links/person.json")

        val link = AuthorizationContext.runWithoutAuthorization {
            processDocumentDeploymentService.deploy("house", linkJson)
            processDocumentAssociationService.findProcessDocumentDefinition(
                CamundaProcessDefinitionId("loan-process-demo")
            ).get()
        }
        assertEquals("house", link.processDocumentDefinitionId().documentDefinitionId().name())
        assertEquals("loan-process-demo", link.processDocumentDefinitionId().processDefinitionKey().toString())
        assertTrue(link.canInitializeDocument())
        assertTrue(link.startableByUser())
    }

}
