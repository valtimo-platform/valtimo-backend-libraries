/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.ritense.zakenapi.uploadprocess.UploadProcessService.Companion.DOCUMENT_UPLOAD
import com.ritense.zakenapi.uploadprocess.UploadProcessService.Companion.RESOURCE_ID_PROCESS_VAR
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.history.HistoricProcessInstance
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import javax.transaction.Transactional

@Transactional
class ResourceUploadedToDocumentEventListenerIT @Autowired constructor(
    private val documentService: JsonSchemaDocumentService,
    private val temporaryResourceStorageService: TemporaryResourceStorageService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val historyService: HistoryService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService
): BaseIntegrationTest() {

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
    fun `should not start upload process when missing documentId or taskId`() {
        val documentId = documentService.createDocument(
            NewDocumentRequest(
                DOCUMENT_DEFINITION_KEY,
                Mapper.INSTANCE.get().createObjectNode(),
            ).withTenantId("1")
        ).resultingDocument().get().id!!.id.toString()
        val resourceId = temporaryResourceStorageService.store("My file data".byteInputStream())

        applicationEventPublisher.publishEvent(TemporaryResourceUploadedEvent(resourceId))

        val documentUploadProcess = historyService.createHistoricProcessInstanceQuery()
            .processInstanceBusinessKey(documentId)
            .singleResult()
        assertThat(documentUploadProcess).isNull()
    }

    @Test
    fun `should start upload process after publishing TemporaryResourceUploadedEvent`() {
        val documentId = documentService.createDocument(
            NewDocumentRequest(
                DOCUMENT_DEFINITION_KEY,
                Mapper.INSTANCE.get().createObjectNode()
            ).withTenantId("1")
        ).resultingDocument().get().id!!.id.toString()
        val resourceId = temporaryResourceStorageService.store(
            "My file data".byteInputStream(),
            mapOf(MetadataType.DOCUMENT_ID.key to documentId)
        )

        applicationEventPublisher.publishEvent(TemporaryResourceUploadedEvent(resourceId))

        val documentUploadProcess = getHistoricProcessInstance(UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY, documentId)
        val retrievedResourceId =
            getHistoricVariable(documentUploadProcess.rootProcessInstanceId, RESOURCE_ID_PROCESS_VAR) as String
        assertThat(documentUploadProcess.startTime).isNotNull
        assertThat(retrievedResourceId).isEqualTo(resourceId)
    }

    private fun getHistoricProcessInstance(processDefinitionKey: String, documentId: String): HistoricProcessInstance {
        return historyService.createHistoricProcessInstanceQuery()
            .processDefinitionKey(processDefinitionKey)
            .processInstanceBusinessKey(documentId)
            .singleResult()
    }

    private fun <T> getHistoricVariable(processInstanceId: String, variableKey: String): T {
        return historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstanceId)
            .variableName(variableKey)
            .singleResult()
            .value as T
    }

    companion object {
        private const val DOCUMENT_DEFINITION_KEY = "profile"
        private const val UPLOAD_DOCUMENT_PROCESS_DEFINITION_KEY = "document-upload"
    }
}
