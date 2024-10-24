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

package com.ritense.zaakdetails.documentobjectenapisync

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.client.ObjectsList
import com.ritense.zaakdetails.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.UUID
import kotlin.test.assertTrue

@Transactional
class DocumentObjectenApiSyncServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var documentObjectenApiSyncService: DocumentObjectenApiSyncService

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should sync new document to Objecten API`() {
        documentObjectenApiSyncService.saveSyncConfiguration(
            DocumentObjectenApiSync(
                documentDefinitionName = "profile",
                documentDefinitionVersion = 1,
                objectManagementConfigurationId = UUID.fromString("462ef788-f7db-4701-9b87-0400fc79ad7e")
            )
        )
        whenever(objectenApiClient.getObjectsByObjecttypeUrlWithSearchParams(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(ObjectsList(0, null, null, listOf()))

        val result = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    "profile",
                    objectMapper.readTree("""{"lastname":"Doe"}""")
                )
            )
        }

        assertTrue { result.errors().isEmpty() }
        verify(objectenApiClient, times(1)).createObject(any(), any(), any())
        verify(objectenApiClient, times(0)).objectUpdate(any(), any(), any())
    }

    @Test
    fun `should sync modified document to Objecten API`() {
        documentObjectenApiSyncService.saveSyncConfiguration(
            DocumentObjectenApiSync(
                documentDefinitionName = "profile",
                documentDefinitionVersion = 1,
                objectManagementConfigurationId = UUID.fromString("462ef788-f7db-4701-9b87-0400fc79ad7e")
            )
        )
        whenever(objectenApiClient.getObjectsByObjecttypeUrlWithSearchParams(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(ObjectsList(0, null, null, listOf()))
        val document = runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(
                    "profile",
                    objectMapper.readTree("""{"lastname":"Deo"}""")
                )
            ).resultingDocument().get()
        }
        whenever(objectenApiClient.getObjectsByObjecttypeUrlWithSearchParams(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(
                ObjectsList(
                    1,
                    null,
                    null,
                    listOf(ObjectWrapper(URI("www.ritense.com"), UUID.randomUUID(), URI("www.ritense.com"), mock()))
                )
            )

        runWithoutAuthorization {
            documentService.modifyDocument(document, objectMapper.readTree("""{"lastname":"Doe"}"""))
        }

        verify(objectenApiClient, times(1)).objectUpdate(any(), any(), any())
    }
}