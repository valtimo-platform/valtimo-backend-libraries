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

package com.ritense.formlink.service.impl;

import com.ritense.formlink.domain.FormLink;
import com.ritense.formlink.domain.FormLinkTaskProvider;
import com.ritense.formlink.domain.TaskOpenResult;
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation;
import com.ritense.formlink.service.FormAssociationService;
import com.ritense.formlink.service.ProcessLinkService;
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.service.CamundaTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultProcessLinkServiceTest {

    private final CamundaTaskService taskService = mock(CamundaTaskService.class, Mockito.RETURNS_DEEP_STUBS);
    private final CamundaRepositoryService repositoryService = mock(CamundaRepositoryService.class);
    private final FormAssociationService formAssociationService = mock(FormAssociationService.class);
    private final FormLinkTaskProvider formLinkTaskProvider = mock(FormLinkTaskProvider.class);
    private final List<FormLinkTaskProvider> formLinkTaskProviders = List.of(formLinkTaskProvider);
    private final ProcessLinkService service = new DefaultProcessLinkService(
        repositoryService,
        taskService,
        formAssociationService,
            formLinkTaskProviders
    );
    CamundaTask task = mock(CamundaTask.class);
    CamundaFormAssociation formAssociation = mock(CamundaFormAssociation.class);
    FormLink formLink = mock(FormLink.class);

    @BeforeEach
    public void beforeEach() {
        when(taskService.findTask(any()))
            .thenReturn(task);
        when(task.getProcessDefinitionId()).thenReturn("test:1");
        when(task.getTaskDefinitionKey()).thenReturn("test");

        doReturn(Optional.of(formAssociation))
            .when(formAssociationService).getFormAssociationByFormLinkId("test", "test");

        when(formAssociation.getFormLink()).thenReturn(formLink);

        when(formLinkTaskProvider.supports(formLink)).thenReturn(true);

        final var processDefinition = mock(CamundaProcessDefinition.class);
        when(processDefinition.getKey()).thenReturn("test");
        when(repositoryService.findProcessDefinitionById("test:1")).thenReturn(processDefinition);
    }

    @Test
    void openTaskShouldGetResultFromProvider() {
        UUID taskId = UUID.randomUUID();
        TaskOpenResult<String> mockResult = new TaskOpenResult<>(
            "test",
            "test"
        );
        when(formLinkTaskProvider.getTaskResult(task, formLink)).thenReturn(mockResult);

        TaskOpenResult taskOpenResult = service.openTask(taskId);

        assertEquals(taskOpenResult, mockResult);
        verify(formLinkTaskProvider).getTaskResult(task, formLink);
    }

    @Test
    void openTaskShouldThrowExceptionWhenAssociationNotFound() {
        UUID taskId = UUID.randomUUID();

        doReturn(Optional.empty())
            .when(formAssociationService).getFormAssociationByFormLinkId("test", "test");

        assertThrows(NoSuchElementException.class, () -> {
            service.openTask(taskId);
        });
    }

    @Test
    void openTaskShouldThrowExceptionWhenNoProviderFound() {
        UUID taskId = UUID.randomUUID();

        when(formLinkTaskProvider.supports(formLink)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> {
            service.openTask(taskId);
        });
    }
}
