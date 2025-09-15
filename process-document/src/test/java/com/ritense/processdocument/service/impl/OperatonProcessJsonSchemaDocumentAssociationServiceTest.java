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

package com.ritense.processdocument.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ritense.authorization.AuthorizationService;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.service.DocumentService;
import com.ritense.processdocument.BaseTest;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository;
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition;
import com.ritense.valtimo.operaton.service.OperatonRepositoryService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import java.util.List;
import java.util.Optional;
import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.history.HistoricProcessInstance;
import org.operaton.bpm.engine.history.HistoricProcessInstanceQuery;

public class OperatonProcessJsonSchemaDocumentAssociationServiceTest extends BaseTest {

    private OperatonProcessJsonSchemaDocumentAssociationService service;
    private ProcessDocumentInstanceRepository processDocumentInstanceRepository;
    private OperatonRepositoryService repositoryService;
    private RuntimeService runtimeService;
    private HistoryService historyService;
    private AuthorizationService authorizationService;
    private DocumentService documentService;
    private UserManagementService userManagementService;

    @BeforeEach
    public void setUp() {
        processDocumentInstanceRepository = spy(ProcessDocumentInstanceRepository.class);
        repositoryService = mock(OperatonRepositoryService.class);
        runtimeService = mock(RuntimeService.class);
        historyService = mock(HistoryService.class);
        authorizationService = mock(AuthorizationService.class);
        documentService = mock(DocumentService.class);
        userManagementService = mock(UserManagementService.class);

        service = new OperatonProcessJsonSchemaDocumentAssociationService(
            processDocumentInstanceRepository,
            repositoryService,
            runtimeService,
            historyService,
            authorizationService,
            documentService,
            userManagementService
        );
    }

    @Test
    public void createProcessDocumentInstance() {
        final var processInstanceId = processInstanceId();
        final var documentId = documentId();

        when(processDocumentInstanceRepository.saveAndFlush(any()))
            .thenReturn(mock(OperatonProcessJsonSchemaDocumentInstance.class));

        service.createProcessDocumentInstance(processInstanceId.toString(), documentId.getId(), "aName");

        verify(processDocumentInstanceRepository).saveAndFlush(any());
    }

    @Test
    public void shouldNotThrowErrorWhenAssociationAlreadyExists() {
        final var processInstanceId = processInstanceId();
        final var documentId = documentId();

        when(processDocumentInstanceRepository.findByProcessInstanceId(any()))
            .thenReturn(Optional.of(new OperatonProcessJsonSchemaDocumentInstance(
                OperatonProcessJsonSchemaDocumentInstanceId.existingId(processInstanceId, documentId),
                "process-name"
            )));
        when(processDocumentInstanceRepository.saveAndFlush(any()))
            .thenReturn(mock(OperatonProcessJsonSchemaDocumentInstance.class));

        var association = service.createProcessDocumentInstance(processInstanceId.toString(), documentId.getId(), "process-name");

        assertThat(association).isPresent();
        assertThat(association.get().isNew()).isFalse();
        assertThat(association.get().processDocumentInstanceId().documentId()).isEqualTo(documentId);
        assertThat(association.get().processDocumentInstanceId().processInstanceId()).isEqualTo(processInstanceId);
        assertThat(association.get().processName()).isEqualTo("process-name");
    }

    @Test
    public void shouldFindProcessInstanceWithFailedResult() {
        final var id = processDocumentInstanceId();

        when(processDocumentInstanceRepository.findById(any())).thenReturn(Optional.empty());

        final FunctionResult<OperatonProcessJsonSchemaDocumentInstance, OperationError> result = service
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

        final var instance = mock(OperatonProcessJsonSchemaDocumentInstance.class);
        when(processDocumentInstanceRepository.findById(any())).thenReturn(Optional.of(instance));

        final FunctionResult<OperatonProcessJsonSchemaDocumentInstance, OperationError> result = service
            .getProcessDocumentInstanceResult(
                OperatonProcessJsonSchemaDocumentInstanceId.existingId(processInstanceId, documentId)
            );

        assertThat(result).isInstanceOf(FunctionResult.Successful.class);
        assertThat(result.hasResult()).isEqualTo(true);
        assertThat(result.isError()).isEqualTo(false);
        assertThat(result.resultingValue().orElseThrow()).isEqualTo(instance);
    }


    @Test
    public void shouldHandleNullOperatonProcessInFindProcessDocumentInstanceDtos() {
        // Given
        final var documentId = documentId();
        final var processInstanceId = processInstanceId();
        final var document = mock(JsonSchemaDocument.class);
        final var processDocumentInstance = mock(OperatonProcessJsonSchemaDocumentInstance.class);
        final var processDocumentInstanceId = OperatonProcessJsonSchemaDocumentInstanceId.existingId(processInstanceId, documentId);
        final var historicProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);

        doReturn(Optional.of(document)).when(documentService).findBy(any());
        when(processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(documentId))
            .thenReturn(List.of(processDocumentInstance));
        when(processDocumentInstance.getId()).thenReturn(processDocumentInstanceId);
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.processInstanceId(processInstanceId.toString())).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.singleResult()).thenReturn(null); // This simulates operatonProcess being null

        // When - should not throw NullPointerException
        final var result = service.findProcessDocumentInstanceDtos(documentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty(); // Should be empty because null entries are filtered out
    }

    @Test
    public void shouldFindProcessDocumentInstanceDtosWithSuccessfulResult() {
        // Given
        final var documentId = documentId();
        final var processInstanceId = processInstanceId();
        final var document = mock(JsonSchemaDocument.class);
        final var processDocumentInstance = mock(OperatonProcessJsonSchemaDocumentInstance.class);
        final var processDocumentInstanceId = OperatonProcessJsonSchemaDocumentInstanceId.existingId(processInstanceId, documentId);
        final var historicProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
        final var historicProcessInstance = mock(HistoricProcessInstance.class);
        final var operatonProcessDefinition = mock(OperatonProcessDefinition.class);
        final var manageableUser = mock(com.ritense.valtimo.contract.authentication.ManageableUser.class);

        // Mock document service
        doReturn(Optional.of(document)).when(documentService).findBy(any());

        // Mock process document instance repository
        when(processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(documentId))
            .thenReturn(List.of(processDocumentInstance));
        when(processDocumentInstance.getId()).thenReturn(processDocumentInstanceId);
        when(processDocumentInstance.processName()).thenReturn("Test Process");
        when(processDocumentInstance.isActive()).thenReturn(true);

        // Mock history service
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.processInstanceId(processInstanceId.toString())).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.singleResult()).thenReturn(historicProcessInstance);

        // Mock historic process instance
        when(historicProcessInstance.getEndTime()).thenReturn(null); // Process is active
        when(historicProcessInstance.getProcessDefinitionKey()).thenReturn("test-process-key");
        when(historicProcessInstance.getStartTime()).thenReturn(java.util.Date.from(java.time.Instant.now()));
        when(historicProcessInstance.getStartUserId()).thenReturn("user@example.com");
        when(historicProcessInstance.getProcessDefinitionVersion()).thenReturn(2);

        // Mock repository service
        when(repositoryService.findLatestProcessDefinition("test-process-key")).thenReturn(operatonProcessDefinition);
        when(operatonProcessDefinition.getVersion()).thenReturn(3);

        // Mock user management service
        when(userManagementService.findByEmail("user@example.com")).thenReturn(Optional.of(manageableUser));
        when(manageableUser.getFullName()).thenReturn("Test User");

        // When
        final var result = service.findProcessDocumentInstanceDtos(documentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        final var dto = result.get(0);
        assertThat(dto.processDocumentInstanceId()).isEqualTo(processDocumentInstanceId);
        assertThat(dto.processName()).isEqualTo("Test Process");
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getVersion()).isEqualTo(2);
        assertThat(dto.getLatestVersion()).isEqualTo(3);
        assertThat(dto.getStartedBy()).isEqualTo("Test User");
        assertThat(dto.getStartedOn()).isNotNull();
    }
}