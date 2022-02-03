/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.smartdocuments.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.domain.impl.Mapper
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.request.FileUploadRequest
import com.ritense.smartdocuments.BaseSmartDocumentsIntegrationTest
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.valtimo.contract.resource.Resource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.mock.mockito.SpyBean
import java.time.LocalDateTime
import java.util.UUID

@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CamundaSmartDocumentGeneratorIntegrationTest : BaseSmartDocumentsIntegrationTest() {

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @SpyBean
    lateinit var smartDocumentGenerator: SmartDocumentGenerator


    private val PROCESS_DEFINITION_KEY = "document-generation"
    private val DOCUMENT_DEFINITION_KEY = "profile"

    @Test
    fun `should generate document & store document`() {
        // given
        val emptyResource = object : Resource {
            override fun id() = UUID.randomUUID()
            override fun name() = "name.txt"
            override fun extension() = "txt"
            override fun sizeInBytes() = 123L
            override fun createdOn() = LocalDateTime.now()
        }
        whenever(resourceService.store(any(), any<FileUploadRequest>())).thenReturn(emptyResource)
        processDocumentAssociationService.createProcessDocumentDefinition(
            ProcessDocumentDefinitionRequest(PROCESS_DEFINITION_KEY, DOCUMENT_DEFINITION_KEY, true, true)
        )
        val jsonContent = Mapper.INSTANCE.get().readTree("{\"lastname\": \"Klaveren\"}")
        val newDocumentRequest = NewDocumentRequest(DOCUMENT_DEFINITION_KEY, jsonContent)
        val request = NewDocumentAndStartProcessRequest(PROCESS_DEFINITION_KEY, newDocumentRequest)
            .withProcessVars(mapOf("age" to 38))

        // when
        processDocumentService.newDocumentAndStartProcess(request)

        // then
        verify(smartDocumentGenerator).generateAndStoreDocument(
            any(),
            eq("my-template-group"),
            eq("my-template-id"),
            eq(mapOf<String, Any>("achternaam" to "Klaveren", "leeftijd" to "38", "toestemming" to "true")),
            eq(DocumentFormatOption.PDF)
        )
        verify(resourceService).store(any(), any<FileUploadRequest>())
    }

}