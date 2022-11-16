/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.service.impl;

import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentService;
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import com.ritense.processdocument.service.impl.result.StartProcessForDocumentResultFailed;
import com.ritense.processdocument.service.impl.result.StartProcessForDocumentResultSucceeded;
import com.ritense.processdocument.service.result.StartProcessForDocumentResult;
import com.ritense.valtimo.camunda.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CamundaProcessJsonSchemaDocumentServiceTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final DocumentDefinitionService documentDefinitionService = mock(DocumentDefinitionService.class);
    private final CamundaTaskService camundaTaskService = mock(CamundaTaskService.class);
    private final CamundaProcessService camundaProcessService = mock(CamundaProcessService.class);
    private final ProcessDocumentAssociationService processDocumentAssociationService = mock(ProcessDocumentAssociationService.class);

    private final ProcessDocumentService processDocumentService = new CamundaProcessJsonSchemaDocumentService(
        documentService,
        documentDefinitionService,
        camundaTaskService,
        camundaProcessService,
        processDocumentAssociationService
    );

    @Test
    void startProcessForDocument_shouldReturnErrorWhenDocumentNotFound() {
        when(documentService.findBy(any())).thenReturn(Optional.empty());

        JsonSchemaDocumentId id = JsonSchemaDocumentId.existingId(UUID.randomUUID());

        StartProcessForDocumentRequest request = new StartProcessForDocumentRequest(
            id,
            "test",
            new HashMap<>()
        );

        StartProcessForDocumentResult result = processDocumentService.startProcessForDocument(request);

        assertTrue(result instanceof StartProcessForDocumentResultFailed);
        assertEquals("Document could not be found", result.errors().get(0).asString());
    }

    @Test
    void startProcessForDocument_shouldNotReturnErrorWhenNoDocumentDefinitionLinkIsFound() {
        Document document = mock(Document.class);
        JsonSchemaDocumentDefinitionId documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId("testdef", 1L);
        FunctionResult processDocumentDefinitionResult = mock(FunctionResult.class);
        JsonSchemaDocumentId id = JsonSchemaDocumentId.existingId(UUID.randomUUID());

        doReturn(Optional.of(document)).when(documentService).findBy(id);
        doReturn(processDocumentDefinitionResult).when(processDocumentAssociationService).getProcessDocumentDefinitionResult(any());
        when(processDocumentDefinitionResult.hasResult()).thenReturn(false);
        when(processDocumentDefinitionResult.errors()).thenReturn(List.of(new OperationError.FromString("error-text")));
        when(document.definitionId()).thenReturn(documentDefinitionId);

        StartProcessForDocumentRequest request = new StartProcessForDocumentRequest(
            id,
            "test",
            new HashMap<>()
        );

        StartProcessForDocumentResult result = processDocumentService.startProcessForDocument(request);

        assertFalse(result instanceof StartProcessForDocumentResultFailed);
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void startProcessForDocument_shouldReturnErrorWhenRuntimeExceptionOccurred() {
        when(documentService.findBy(any())).thenThrow(new RuntimeException("error"));

        JsonSchemaDocumentId id = JsonSchemaDocumentId.existingId(UUID.randomUUID());

        StartProcessForDocumentRequest request = new StartProcessForDocumentRequest(
            id,
            "test",
            new HashMap<>()
        );

        StartProcessForDocumentResult result = processDocumentService.startProcessForDocument(request);

        assertTrue(result instanceof StartProcessForDocumentResultFailed);
        assertTrue(result.errors().get(0).asString().startsWith("Unexpected error occurred, please contact support - referenceId:"));
    }

    @Test
    void startProcessForDocument_shouldReturnSuccessWhenProcessWasStarted() {
        JsonSchemaDocumentDefinitionId documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId("testdef", 1L);
        FunctionResult processDocumentDefinitionResult = mock(FunctionResult.class);

        Document document = mock(Document.class);
        UUID documentUuid = UUID.randomUUID();
        JsonSchemaDocumentId id = JsonSchemaDocumentId.existingId(documentUuid);
        when(document.id()).thenReturn(id);

        ProcessInstance processInstance = mock(ProcessInstance.class);
        String processInstanceId = UUID.randomUUID().toString();
        when(processInstance.getId()).thenReturn(processInstanceId);

        ProcessDefinition processDefinition = mock(ProcessDefinition.class);
        when(processDefinition.getName()).thenReturn("test-name");

        doReturn(Optional.of(document)).when(documentService).findBy(id);
        doReturn(processDocumentDefinitionResult).when(processDocumentAssociationService).getProcessDocumentDefinitionResult(any());
        when(processDocumentDefinitionResult.hasResult()).thenReturn(true);
        when(document.definitionId()).thenReturn(documentDefinitionId);

        Map<String, Object> processVars = new HashMap<>();

        ProcessInstanceWithDefinition processInstanceWithDefinition = new ProcessInstanceWithDefinition(processInstance,
            processDefinition);
        when(camundaProcessService.startProcess("test-name", documentUuid.toString(), processVars))
            .thenReturn(processInstanceWithDefinition);

        StartProcessForDocumentRequest request = new StartProcessForDocumentRequest(
            id,
            "test-name",
            processVars
        );

        StartProcessForDocumentResult result = processDocumentService.startProcessForDocument(request);

        assertTrue(result instanceof StartProcessForDocumentResultSucceeded);

        verify(camundaProcessService).startProcess(
            "test-name",
            documentUuid.toString(),
            processVars
        );

        verify(processDocumentAssociationService).createProcessDocumentInstance(
            processInstanceId,
            documentUuid,
            "test-name"
        );
    }
}
