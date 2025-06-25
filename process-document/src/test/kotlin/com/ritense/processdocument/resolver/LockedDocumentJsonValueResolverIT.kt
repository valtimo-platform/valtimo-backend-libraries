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

package com.ritense.processdocument.resolver

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.BaseIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.CountDownLatch

internal class LockedDocumentJsonValueResolverIT: BaseIntegrationTest() {

    @Autowired
    lateinit var  documentValueResolver: LockedDocumentJsonValueResolverFactory

    @Autowired
    lateinit var  documentService: JsonSchemaDocumentService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var someTestBean: SomeTestBean

    @BeforeEach
    internal fun setUp() {

    }

    @Test
    fun `simple concurrent updates`() {
        val documentJson =
            """
            {
                "street": "aStreet",
                "houseNumber": 1
            }
            """.trimIndent()

        val document = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest("house", objectMapper.readTree(documentJson))
            ).resultingDocument().orElseThrow()
        }

        val latch = CountDownLatch(1)
        val done = CountDownLatch(2)

        Thread {
            latch.await()
            someTestBean.updateDocument(document)
            done.countDown()
        }.start()

        Thread {
            latch.await()
            someTestBean.updateDocument(document)
            done.countDown()
        }.start()

        latch.countDown()
        done.await()
    }

    @Test
    fun `simple find and update`() {
        val documentJson =
            """
            {
                "street": "aStreet",
                "houseNumber": 1
            }
            """.trimIndent()

        val document = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest("house", objectMapper.readTree(documentJson))
            ).resultingDocument().orElseThrow()
        }

        val latch = CountDownLatch(1)
        val done = CountDownLatch(2)

        Thread {
            latch.await()
            someTestBean.findAndUpdateDocument(document)
            done.countDown()
        }.start()

        Thread {
            latch.await()
            someTestBean.findAndUpdateDocument(document)
            done.countDown()
        }.start()

        latch.countDown()
        done.await()
    }
}
