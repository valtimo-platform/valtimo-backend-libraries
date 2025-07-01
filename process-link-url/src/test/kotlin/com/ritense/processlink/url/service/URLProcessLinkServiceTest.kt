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

package com.ritense.processlink.url.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.ValtimoAuthorizationService
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processdocument.service.result.DocumentFunctionResult
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.processlink.url.domain.URLProcessLink
import com.ritense.processlink.url.domain.URLVariables
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import com.ritense.valtimo.operaton.service.OperatonRepositoryService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.service.OperatonTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

class URLProcessLinkServiceTest {

    @Mock
    lateinit var processLinkService: ProcessLinkService

    @Mock
    lateinit var documentService: JsonSchemaDocumentService

    @Mock
    lateinit var processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService

    @Mock
    lateinit var processDocumentService: ProcessDocumentService

    @Mock
    lateinit var repositoryService: OperatonRepositoryService

    @Mock
    lateinit var objectMapper: ObjectMapper

    @Mock
    lateinit var urlVariables: URLVariables

    @Mock
    lateinit var operatonTaskService: OperatonTaskService

    @Mock
    lateinit var authorizationService: ValtimoAuthorizationService

    @InjectMocks
    lateinit var urlProcessLinkService: URLProcessLinkService

    @BeforeEach
    fun beforeEach() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun submit() {
        val processLinkId = UUID.randomUUID()
        val documentId = UUID.randomUUID().toString()

        val processLink = mock<URLProcessLink>()
        val processLinkDefinitionId = UUID.randomUUID().toString()
        whenever(processLink.processDefinitionId).thenReturn(processLinkDefinitionId)
        whenever(processLink.activityType).thenReturn(ActivityTypeWithEventName.USER_TASK_CREATE)
        whenever(processLinkService.getProcessLink(processLinkId, URLProcessLink::class.java))
            .thenReturn(processLink)

        val document = mock<JsonSchemaDocument>()
        whenever(document.definitionId()).thenReturn(JsonSchemaDocumentDefinitionId.existingId("name", CaseDefinitionId("caseDefinitionName", "1.0.0")))
        whenever(document.id()).thenReturn(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))
        whenever(documentService.get(documentId)).thenReturn(document)

        val operatonProcessDefinition = mock<OperatonProcessDefinition>()
        whenever(repositoryService.findProcessDefinitionById(processLinkDefinitionId))
            .thenReturn(operatonProcessDefinition)

        val resultSucceeded = mock<DocumentFunctionResult<JsonSchemaDocument>>()
        whenever(resultSucceeded.errors()).thenReturn(emptyList())
        whenever(resultSucceeded.resultingDocument()).thenReturn(Optional.of(document))
        whenever(processDocumentService.dispatch(any()))
            .thenReturn(resultSucceeded)

        urlProcessLinkService.submit(
            processLinkId,
            "caseDefinitionName",
            documentId,
            "taskInstanceId"
        )

        verify(processDocumentService).dispatch(any<ModifyDocumentAndCompleteTaskRequest>())
    }
}