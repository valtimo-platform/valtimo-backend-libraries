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

package com.ritense.smartdocuments.plugin

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.smartdocuments.BaseSmartDocumentsIntegrationTest
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.contract.resource.Resource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpMethod
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat

@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SmartDocumentsPluginIntegrationTest : BaseSmartDocumentsIntegrationTest() {

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var processDocumentService: ProcessDocumentService

    @Autowired
    lateinit var pluginService: PluginService

    @Autowired
    lateinit var smartDocumentsPluginFactory: SmartDocumentsPluginFactory

    lateinit var smartDocumentsPlugin: SmartDocumentsPlugin

    @BeforeEach
    internal fun beforeEach() {
        startMockServer()
        val configuration = pluginService.createPluginConfiguration(
            "smart-documents-plugin-configuration",
            "Smart documents plugin configuration",
            "{\"url\":\"${server.url("/")}\",\"username\":\"test-username\",\"password\":\"test-password\"}",
            "smartdocuments"
        )
        smartDocumentsPlugin = smartDocumentsPluginFactory.create(configuration.key)
    }

    @Test
    fun `should generate document`() {
        // given
        val emptyResource = object : Resource {
            override fun id() = UUID.randomUUID()
            override fun name() = "name.txt"
            override fun extension() = "txt"
            override fun sizeInBytes() = 123L
            override fun createdOn() = LocalDateTime.now()
        }
        whenever(resourceService.store(any(), any(), any<MultipartFile>())).thenReturn(emptyResource)
        processDocumentAssociationService.createProcessDocumentDefinition(
            ProcessDocumentDefinitionRequest(PROCESS_DEFINITION_KEY, DOCUMENT_DEFINITION_KEY, true, true)
        )
        val documentContent = Mapper.INSTANCE.get().readTree("{\"lastname\": \"Klaveren\"}")
        val newDocumentRequest = NewDocumentRequest(DOCUMENT_DEFINITION_KEY, documentContent)
        val request = NewDocumentAndStartProcessRequest(PROCESS_DEFINITION_KEY, newDocumentRequest)
            .withProcessVars(mapOf("age" to 138))

        // when
        processDocumentService.newDocumentAndStartProcess(request)

        // then
        val requestBody = findRequestBody(HttpMethod.POST, "/wsxmldeposit/deposit/unattended", SmartDocumentsRequest::class.java)
        assertThat(requestBody.smartDocument.selection.templateGroup).isEqualTo("test-template-group")
        assertThat(requestBody.smartDocument.selection.template).isEqualTo("test-template-name")
        assertThat(requestBody.customerData).isEqualTo(mapOf("achternaam" to "Klaveren", "leeftijd" to "138"))
    }

    companion object {
        private val PROCESS_DEFINITION_KEY = "document-generation-plugin"
        private val DOCUMENT_DEFINITION_KEY = "profile"
    }

}
