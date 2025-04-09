/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Transactional
class ValueResolverDelegateServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = [AuthoritiesConstants.USER])
    fun `should copy jsonObject in process variable to document`() {
        val processDefinitionKey = "pv-object-to-doc-process"
        val documentDefinitionName = "additional-properties"
        val processVars = mapOf("person" to mapOf("firstName" to "John", "lastName" to "Doe"))
        val documentContent = objectMapper.createObjectNode()

        val result = runWithoutAuthorization {
            processDocumentService.newDocumentAndStartProcess(
                NewDocumentAndStartProcessRequest(
                    processDefinitionKey,
                    NewDocumentRequest(documentDefinitionName, documentContent)
                ).withProcessVars(processVars)
            )
        }

        assertTrue(result.errors().isEmpty())
        assertEquals(
            """{"person":{"firstName":"John","lastName":"Doe"}}""",
            result.resultingDocument().get().content().asJson().toString()
        )
    }
}