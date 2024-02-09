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

package com.ritense.processdocument.camunda.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CamundaTaskDocumentMapperIntTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [AuthoritiesConstants.USER])
    fun `should map CamundaTask to Document`() {
        val result = runWithoutAuthorization {
            processDocumentService.newDocumentAndStartProcess(
                NewDocumentAndStartProcessRequest(
                    "single-user-task-process",
                    NewDocumentRequest(
                        "house",
                        objectMapper.readTree("""{"city":"Amsterdam"}""")
                    )
                )
            )
        }

        val task = camundaTaskService.findTask(
            CamundaTaskSpecificationHelper.byProcessInstanceId(
                result.resultingProcessInstanceId().get().toString()
            )
        )

        assertEquals("My user task", task.name)
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [AuthoritiesConstants.USER])
    fun `should NOT map CamundaTask to Document with unmapped city`() {
        val result = runWithoutAuthorization {
            processDocumentService.newDocumentAndStartProcess(
                NewDocumentAndStartProcessRequest(
                    "single-user-task-process",
                    NewDocumentRequest(
                        "house",
                        objectMapper.readTree("""{"city":"Utrecht"}""")
                    )
                )
            )
        }

        val task = camundaTaskService.findTask(
            CamundaTaskSpecificationHelper.byProcessInstanceId(
                result.resultingProcessInstanceId().get().toString()
            )
        )

        assertNull(task)
    }
}