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

package com.ritense.processdocument.other

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.BaseIntegrationTest
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

@Tag("integration")
internal class DeadlockIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var documentService: DocumentService

    @BeforeEach
    fun beforeEach() {
        val admin = ValtimoUser()
        admin.id = USERNAME
        admin.username = USERNAME
        admin.roles = listOf(USER, ADMIN)
        whenever(userManagementService.currentUser).thenReturn(admin)
        whenever(userManagementService.findByUserIdentifier(USERNAME)).thenReturn(admin)
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should not deadlock`(): Unit = runBlocking {
        val document = createDocument("""{"street": "Main Thread"}""")
        val threads = mutableListOf<Deferred<Any>>()

        repeat(100) { i ->
            threads.add(async(Dispatchers.IO) {
                runWithoutAuthorization {
                    logger.info { "Thread: $i" }
                    camundaProcessJsonSchemaDocumentService.startProcessForDocument(
                        StartProcessForDocumentRequest(
                            document.id,
                            "deadlock-process",
                            mapOf()
                        )
                    )
                }
            })
        }

        threads.awaitAll()
    }

    private fun createDocument(content: String): JsonSchemaDocument {
        return runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    definition().id().name(),
                    JsonDocumentContent(content).asJson()
                )
            )
        }.resultingDocument().orElseThrow() as JsonSchemaDocument
    }


    companion object {
        private const val USERNAME = "john@ritense.com"
        val logger = KotlinLogging.logger {}
    }
}
