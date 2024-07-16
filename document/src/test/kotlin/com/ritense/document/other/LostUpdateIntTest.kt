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

package com.ritense.document.other

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.security.test.context.support.WithMockUser
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class LostUpdateIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var lostUpdateService: LostUpdateService

    @BeforeEach
    fun beforeEach() {
        val admin = ValtimoUser()
        admin.id = USERNAME
        admin.username = USERNAME
        admin.roles = listOf(USER, ADMIN)
        whenever(userManagementService.currentUser).thenReturn(admin)
        whenever(userManagementService.findByUserIdentifier(USERNAME)).thenReturn(admin)
    }

    @AfterEach
    override fun afterEach() {
        documentRepository.deleteAll()
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = [ADMIN])
    fun `should throw when Lost Update`(): Unit = runBlocking {
        val documentId = createDocument("""{}""").id

        val exceptions = (1..100).map { i ->
            async(Dispatchers.IO) {
                captureException {
                    lostUpdateService.writeDocumentContent(documentId, i)
                }
            }
        }.awaitAll()
            .filterNotNull()

        exceptions.forEach { exception ->
            assertTrue(exception is OptimisticLockingFailureException)
        }
        val modifiedDocument = runWithoutAuthorization { documentService.get(documentId.id.toString()) }
        val numOfSuccessfulWrites = modifiedDocument.content().asJson().toString().split("index_").size - 1
        val numOfFailedWrites = exceptions.size
        assertEquals(100, numOfFailedWrites + numOfSuccessfulWrites)
    }

    private fun createDocument(content: String): JsonSchemaDocument {
        return runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    definitionOf("allows-all").id().name(),
                    JsonDocumentContent(content).asJson()
                )
            )
        }.resultingDocument().orElseThrow() as JsonSchemaDocument
    }

    private fun captureException(executable: () -> Unit): Throwable? {
        return try {
            executable()
            null
        } catch (caught: Throwable) {
            logger.debug { caught }
            caught
        }
    }

    private fun Throwable.getRootCause(): Throwable {
        var cause = this
        while (cause.cause != null) {
            cause = cause.cause!!
        }
        return cause
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val USERNAME = "john@ritense.com"
    }
}