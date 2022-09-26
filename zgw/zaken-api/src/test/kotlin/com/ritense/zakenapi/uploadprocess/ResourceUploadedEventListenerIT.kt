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

package com.ritense.zakenapi.uploadprocess

import com.ritense.document.domain.impl.Mapper
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.resource.domain.MetadataType
import com.ritense.resource.domain.TemporaryResourceUploadedEvent
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.zakenapi.BaseIntegrationTest
import com.ritense.zakenapi.uploadprocess.ResourceUploadedEventListener.Companion.DOCUMENT_UPLOAD
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.HistoryService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import javax.transaction.Transactional

@Transactional
class ResourceUploadedEventListenerIT : BaseIntegrationTest() {

    @Autowired
    lateinit var documentService: JsonSchemaDocumentService

    @Autowired
    lateinit var temporaryResourceStorageService: TemporaryResourceStorageService

    @Autowired
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Autowired
    lateinit var historyService: HistoryService

    @Autowired
    lateinit var processDocumentAssociationService: ProcessDocumentAssociationService

    @Autowired
    lateinit var documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService

    @BeforeEach
    fun beforeEach() {
        processDocumentAssociationService.createProcessDocumentDefinition(
            ProcessDocumentDefinitionRequest(
                UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY,
                DOCUMENT_DEFINITION_KEY,
                true
            )
        )
        documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            DOCUMENT_DEFINITION_KEY,
            DocumentDefinitionProcessRequest(
                UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY,
                DOCUMENT_UPLOAD
            )
        )
    }

    @Test
    fun `should start process after publishing TemporaryResourceUploadedEvent`() {
        val documentId = documentService.createDocument(
            NewDocumentRequest(
                DOCUMENT_DEFINITION_KEY,
                Mapper.INSTANCE.get().createObjectNode()
            )
        ).resultingDocument().get().id!!.id.toString()
        val resourceId = temporaryResourceStorageService.store(
            "My file data".byteInputStream(),
            mapOf(
                MetadataType.DOCUMENT_ID.key to documentId,
            ),
        )

        applicationEventPublisher.publishEvent(TemporaryResourceUploadedEvent(resourceId))

        val documentUploadProcess = historyService.createHistoricProcessInstanceQuery()
            .processInstanceBusinessKey(documentId)
            .singleResult()
        val retrievedResourcesId = historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(documentUploadProcess.rootProcessInstanceId)
            .variableName(ResourceUploadedEventListener.RESOURCE_ID_PROCESS_VAR)
            .singleResult()
            .value as String
        assertThat(documentUploadProcess.startTime).isNotNull
        assertThat(retrievedResourcesId).isEqualTo(resourceId)
    }

    companion object {
        private const val DOCUMENT_DEFINITION_KEY = "profile"
        private const val UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY = "document-upload"
    }
}
