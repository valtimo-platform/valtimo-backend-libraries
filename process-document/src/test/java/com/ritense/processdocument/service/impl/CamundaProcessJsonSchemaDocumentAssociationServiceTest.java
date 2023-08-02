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

package com.ritense.processdocument.service.impl;

import com.ritense.authorization.AuthorizationService;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.DocumentDefinitionRepository;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.processdocument.BaseTest;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinition;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.exception.UnknownProcessDefinitionException;
import com.ritense.processdocument.repository.ProcessDocumentDefinitionRepository;
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CamundaProcessJsonSchemaDocumentAssociationServiceTest extends BaseTest {

    private CamundaProcessJsonSchemaDocumentAssociationService service;
    private ProcessDocumentDefinitionRepository processDocumentDefinitionRepository;
    private ProcessDocumentInstanceRepository processDocumentInstanceRepository;
    private DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository;
    private DocumentDefinitionService<JsonSchemaDocumentDefinition> documentDefinitionService;
    private CamundaRepositoryService repositoryService;
    private RuntimeService runtimeService;
    private AuthorizationService authorizationService;
    private DocumentService<JsonSchemaDocument> documentService;

    @BeforeEach
    public void setUp() {
        processDocumentDefinitionRepository = spy(ProcessDocumentDefinitionRepository.class);
        processDocumentInstanceRepository = spy(ProcessDocumentInstanceRepository.class);
        documentDefinitionRepository = mock(DocumentDefinitionRepository.class);
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        repositoryService = mock(CamundaRepositoryService.class);
        runtimeService = mock(RuntimeService.class);
        authorizationService = mock(AuthorizationService.class);
        documentService = mock(JsonSchemaDocumentService.class);

        service = new CamundaProcessJsonSchemaDocumentAssociationService(
            processDocumentDefinitionRepository,
            processDocumentInstanceRepository,
            documentDefinitionRepository,
            documentDefinitionService,
            repositoryService,
            runtimeService,
            authorizationService,
            documentService
        );
    }

    @Test
    public void shouldCreateProcessDocumentDefinition() {
        final var processDefinitionKey = processDefinitionKey();
        final var definitionId = definitionId();
        final var processDocumentRequest = new ProcessDocumentDefinitionRequest(
            processDefinitionKey.toString(),
            definitionId.name(),
            true,
            true
        );

        when(repositoryService.processDefinitionExists(any()))
            .thenReturn(true);

        when(documentDefinitionService.findIdByName(anyString()))
            .thenReturn(definitionId);

        when(documentDefinitionRepository.existsById(any(JsonSchemaDocumentDefinitionId.class)))
            .thenReturn(true);

        when(processDocumentDefinitionRepository.saveAndFlush(any()))
            .thenReturn(mock(CamundaProcessJsonSchemaDocumentDefinition.class));

        service.createProcessDocumentDefinition(processDocumentRequest);

        verify(processDocumentDefinitionRepository).saveAndFlush(any());
    }

    @Test
    public void shouldNotCreateProcessDocumentDefinitionUnknownProcess() {
        final var processDefinitionKey = processDefinitionKey();
        final var definitionId = definitionId();

        final var processDocumentDefinitionRequest = new ProcessDocumentDefinitionRequest(
            processDefinitionKey.toString(),
            definitionId.name(),
            true,
            true
        );

        when(documentDefinitionService.findIdByName(anyString()))
            .thenReturn(definitionId);

        when(repositoryService.processDefinitionExists(any()))
            .thenReturn(false);

        assertThrows(UnknownProcessDefinitionException.class, () -> {
            service.createProcessDocumentDefinition(processDocumentDefinitionRequest);
        });
    }

    @Test
    public void shouldNotCreateProcessDocumentDefinitionUnknownDocument() {
        final var processDefinitionKey = processDefinitionKey();
        final var definitionId = definitionId();
        final var processDocumentRequest = new ProcessDocumentDefinitionRequest(
            processDefinitionKey.toString(),
            definitionId.name(),
            true,
            true
        );

        when(repositoryService.processDefinitionExists(any()))
            .thenReturn(true);

        when(documentDefinitionService.findIdByName(anyString()))
            .thenReturn(definitionId);

        when(documentDefinitionRepository.existsById(any(JsonSchemaDocumentDefinitionId.class)))
            .thenReturn(false);

        assertThrows(UnknownDocumentDefinitionException.class, () -> {
            service.createProcessDocumentDefinition(processDocumentRequest);
        });
    }

    @Test
    public void createProcessDocumentInstance() {
        final var processInstanceId = processInstanceId();
        final var documentId = documentId();

        when(processDocumentInstanceRepository.saveAndFlush(any()))
            .thenReturn(mock(CamundaProcessJsonSchemaDocumentInstance.class));

        service.createProcessDocumentInstance(processInstanceId.toString(), documentId.getId(), "aName");

        verify(processDocumentInstanceRepository).saveAndFlush(any());
    }

    @Test
    public void shouldFindProcessInstanceWithFailedResult() {
        final var id = processDocumentInstanceId();

        when(processDocumentInstanceRepository.findById(any())).thenReturn(Optional.empty());

        final FunctionResult<CamundaProcessJsonSchemaDocumentInstance, OperationError> result = service
            .getProcessDocumentInstanceResult(id);

        assertThat(result).isInstanceOf(FunctionResult.Erroneous.class);
        assertThat(result.hasResult()).isEqualTo(false);
        assertThat(result.isError()).isEqualTo(true);
        assertThat(result.resultingValue().isEmpty()).isEqualTo(true);
    }

    @Test
    public void shouldFindProcessInstanceWithSuccessResult() {
        final var processInstanceId = processInstanceId();
        final var documentId = documentId();

        final var instance = mock(CamundaProcessJsonSchemaDocumentInstance.class);
        when(processDocumentInstanceRepository.findById(any())).thenReturn(Optional.of(instance));

        final FunctionResult<CamundaProcessJsonSchemaDocumentInstance, OperationError> result = service
            .getProcessDocumentInstanceResult(
                CamundaProcessJsonSchemaDocumentInstanceId.existingId(processInstanceId, documentId)
            );

        assertThat(result).isInstanceOf(FunctionResult.Successful.class);
        assertThat(result.hasResult()).isEqualTo(true);
        assertThat(result.isError()).isEqualTo(false);
        assertThat(result.resultingValue().orElseThrow()).isEqualTo(instance);
    }

    @Test
    public void shouldDeleteDocumentDefinition() {
        final var id = processDocumentDefinitionId();
        final var request = new ProcessDocumentDefinitionRequest(
            id.processDefinitionKey().toString(),
            id.documentDefinitionId().name(),
            true,
            true
        );

        when(documentDefinitionService.findIdByName(anyString())).thenReturn(id.documentDefinitionId());

        service.deleteProcessDocumentDefinition(request);
        verify(processDocumentDefinitionRepository).deleteById(id);
    }

}