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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ritense.authorization.AuthorizationService;
import com.ritense.document.service.DocumentService;
import com.ritense.processdocument.BaseTest;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import java.util.Optional;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CamundaProcessJsonSchemaDocumentAssociationServiceTest extends BaseTest {

    private CamundaProcessJsonSchemaDocumentAssociationService service;
    private ProcessDocumentInstanceRepository processDocumentInstanceRepository;
    private CamundaRepositoryService repositoryService;
    private RuntimeService runtimeService;
    private HistoryService historyService;
    private AuthorizationService authorizationService;
    private DocumentService documentService;
    private UserManagementService userManagementService;

    @BeforeEach
    public void setUp() {
        processDocumentInstanceRepository = spy(ProcessDocumentInstanceRepository.class);
        repositoryService = mock(CamundaRepositoryService.class);
        runtimeService = mock(RuntimeService.class);
        historyService = mock(HistoryService.class);
        authorizationService = mock(AuthorizationService.class);
        documentService = mock(DocumentService.class);
        userManagementService = mock(UserManagementService.class);

        service = new CamundaProcessJsonSchemaDocumentAssociationService(
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
            .thenReturn(mock(CamundaProcessJsonSchemaDocumentInstance.class));

        service.createProcessDocumentInstance(processInstanceId.toString(), documentId.getId(), "aName");

        verify(processDocumentInstanceRepository).saveAndFlush(any());
    }

    @Test
    public void shouldNotThrowErrorWhenAssociationAlreadyExists() {
        final var processInstanceId = processInstanceId();
        final var documentId = documentId();

        when(processDocumentInstanceRepository.findByProcessInstanceId(any()))
            .thenReturn(Optional.of(new CamundaProcessJsonSchemaDocumentInstance(
                CamundaProcessJsonSchemaDocumentInstanceId.existingId(processInstanceId, documentId),
                "process-name"
            )));
        when(processDocumentInstanceRepository.saveAndFlush(any()))
            .thenReturn(mock(CamundaProcessJsonSchemaDocumentInstance.class));

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
}