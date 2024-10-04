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

package com.ritense.valtimo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.specification.AuthorizationSpecification;
import com.ritense.outbox.OutboxService;
import com.ritense.outbox.domain.BaseEvent;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.repository.CamundaTaskRepository;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.json.MapperSingleton;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.security.exceptions.TaskNotFoundException;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;

class CamundaTaskServiceTest {
    private static final String TASK_ID = "task";
    private static final String ASSIGNEE = "AAAA-1111";
    private CamundaTask task;
    private TaskService taskService;
    private FormService formService;
    private DelegateTaskHelper delegateTaskHelper;
    private CamundaTaskRepository camundaTaskRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private CamundaTaskService camundaTaskService;
    private RuntimeService runtimeService;
    private UserManagementService userManagementService;
    private EntityManager entityManager;
    private AuthorizationService authorizationService;
    private OutboxService outboxService;
    private final ObjectMapper objectMapper = MapperSingleton.INSTANCE.get();

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class, RETURNS_DEEP_STUBS);
        formService = mock(FormService.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        runtimeService = mock(RuntimeService.class);
        delegateTaskHelper = mock(DelegateTaskHelper.class);
        camundaTaskRepository = mock(CamundaTaskRepository.class);
        userManagementService = mock(UserManagementService.class);
        entityManager = mock(EntityManager.class);
        authorizationService = mock(AuthorizationService.class);
        outboxService = mock(OutboxService.class);
        task = spy(
            new CamundaTask(
                TASK_ID,
                0,
                null,
                null,
                null,
                List.of(),
                null,
                null,
                null,
                "Some task",
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                LocalDateTime.now(),
                null,
                null,
                null,
                0,
                null,
                Set.of()
            )
        );
        camundaTaskService = spy(
            new CamundaTaskService(
                taskService,
                formService,
                delegateTaskHelper,
                camundaTaskRepository,
                null,
                Optional.empty(),
                applicationEventPublisher,
                runtimeService,
                userManagementService,
                entityManager,
                authorizationService,
                outboxService,
                objectMapper
            )
        );
        when(authorizationService.getAuthorizationSpecification(any(), any()))
            .thenReturn(mock(AuthorizationSpecification.class));
    }

    @Test
    void findTaskById_taskDoesNotExists() {
        assertThrows(TaskNotFoundException.class, () -> camundaTaskService.findTaskById(TASK_ID));
    }

    @Test
    void assignTask_taskDoesNotExist() {
        ManageableUser manageableUser = mock();
        when(camundaTaskRepository.findById(any())).thenReturn(Optional.empty());
        when(userManagementService.findById(ASSIGNEE)).thenReturn(manageableUser);
        assertThrows(TaskNotFoundException.class, () -> camundaTaskService.assign(TASK_ID, ASSIGNEE));
    }

    @Test
    void assignTask_taskExists() {
        ProcessInstanceQuery processInstanceQueryMock = mock(ProcessInstanceQuery.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);

        when(camundaTaskRepository.findById(any()))
            .thenReturn(Optional.of(task));
        doReturn("1").when(task).getProcessInstanceId();
        doReturn("2").when(task).getProcessDefinitionId();
        doReturn(task).when(camundaTaskService).findTaskById(TASK_ID);

        ManageableUser manageableUser = mock();
        when(manageableUser.getUserIdentifier()).thenReturn(ASSIGNEE);
        when(userManagementService.findById(ASSIGNEE)).thenReturn(manageableUser);
        ManageableUser currentUser = mock();
        when(currentUser.getUserIdentifier()).thenReturn("TEST");
        when(userManagementService.getCurrentUser()).thenReturn(currentUser);

        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQueryMock);
        when(processInstanceQueryMock.processInstanceId("1")).thenReturn(processInstanceQueryMock);
        when(processInstanceQueryMock.singleResult()).thenReturn(processInstance);

        when(processInstance.getBusinessKey()).thenReturn("123");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn("Henk");

            camundaTaskService.assign(TASK_ID, ASSIGNEE);
        }

        ArgumentCaptor<Supplier<BaseEvent>> eventCapture = ArgumentCaptor.forClass(Supplier.class);
        verify(outboxService, times(1)).send(eventCapture.capture());
        var event = eventCapture.getValue().get();
        assertThat(event.getType()).isEqualTo("com.ritense.valtimo.task.assigned");
        assertThat(event.getResultType()).isEqualTo("com.ritense.valtimo.camunda.domain.CamundaTask");
        assertThat(event.getResultId()).isEqualTo(task.getId());
        assertThat(event.getResult()).isNotNull();
    }

    @Test
    void assignTask_taskExistsWithoutNewAssignee() {
        ProcessInstanceQuery processInstanceQueryMock = mock(ProcessInstanceQuery.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);

        when(camundaTaskRepository.findById(any()))
            .thenReturn(Optional.of(task));
        doReturn("1").when(task).getProcessInstanceId();
        doReturn("2").when(task).getProcessDefinitionId();
        doReturn(task).when(camundaTaskService).findTaskById(TASK_ID);

        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQueryMock);
        when(processInstanceQueryMock.processInstanceId("1")).thenReturn(processInstanceQueryMock);
        when(processInstanceQueryMock.singleResult()).thenReturn(processInstance);

        when(processInstance.getBusinessKey()).thenReturn("123");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn("Henk");

            camundaTaskService.assign(TASK_ID, null);
        }

        ArgumentCaptor<Supplier<BaseEvent>> eventCapture = ArgumentCaptor.forClass(Supplier.class);
        verify(outboxService, times(1)).send(eventCapture.capture());
        var event = eventCapture.getValue().get();
        assertThat(event.getType()).isEqualTo("com.ritense.valtimo.task.unassigned");
        assertThat(event.getResultType()).isEqualTo("com.ritense.valtimo.camunda.domain.CamundaTask");
        assertThat(event.getResultId()).isEqualTo(task.getId());
        assertThat(event.getResult()).isNotNull();
    }

    @Test
    void assignTask_taskClaimedDoesntExist() {
        //when
        when(camundaTaskRepository.findById(any())).thenReturn(Optional.of(
            task));
        doThrow(new ProcessEngineException()).when(taskService).setAssignee(eq(TASK_ID), eq(ASSIGNEE));
        doReturn(task).when(camundaTaskService).findTaskById(TASK_ID);

        ManageableUser manageableUser = mock();
        when(manageableUser.getUserIdentifier()).thenReturn(ASSIGNEE);
        when(userManagementService.findById(ASSIGNEE)).thenReturn(manageableUser);
        ManageableUser currentUser = mock();
        when(currentUser.getUserIdentifier()).thenReturn("TEST");
        when(userManagementService.getCurrentUser()).thenReturn(currentUser);

        assertThrows(IllegalStateException.class, () -> camundaTaskService.assign(TASK_ID, ASSIGNEE));
        verify(outboxService, times(0)).send(any());
    }

    @Test
    void assignTask_taskClaimedWithNoPermissions() {
        when(camundaTaskRepository.findById(any())).thenReturn(Optional.of(
            task));
        ManageableUser manageableUser = mock();
        when(manageableUser.getUserIdentifier()).thenReturn(ASSIGNEE);
        when(userManagementService.findById(ASSIGNEE)).thenReturn(manageableUser);
        ManageableUser currentUser = mock();
        when(currentUser.getUserIdentifier()).thenReturn("TEST");
        when(userManagementService.getCurrentUser()).thenReturn(currentUser);
        doThrow(new AuthorizationException("some reason")).when(taskService).setAssignee(eq(TASK_ID), eq(ASSIGNEE));
        assertThrows(IllegalStateException.class, () -> camundaTaskService.assign(TASK_ID, ASSIGNEE));
        verify(outboxService, times(0)).send(any());
    }

    @Test
    void unassignTask_taskDoesNotExist() {
        doThrow(new IllegalStateException()).when(camundaTaskService).findTaskById(TASK_ID);
        assertThrows(IllegalStateException.class, () -> camundaTaskService.unassign(TASK_ID));
        verify(outboxService, times(0)).send(any());
    }

    @Test
    void unassignTask_taskUnclaimedDoesntExist() {
        when(camundaTaskRepository.findById(any())).thenReturn(Optional.of(
            task));
        doThrow(new ProcessEngineException()).when(taskService).setAssignee(eq(TASK_ID), isNull());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.unassign(TASK_ID));
        verify(outboxService, times(0)).send(any());
    }

    @Test
    void unassignTask_taskClaimedWithNoPermissions() {
        when(camundaTaskRepository.findById(any())).thenReturn(Optional.of(
            task));
        doThrow(new AuthorizationException("some reason")).when(taskService).setAssignee(eq(TASK_ID), isNull());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.unassign(TASK_ID));
        verify(outboxService, times(0)).send(any());
    }

    @Test
    void unassignTask_taskExists() {
        doReturn(task).when(camundaTaskService).findTaskById(TASK_ID);

        camundaTaskService.unassign(TASK_ID);

        ArgumentCaptor<Supplier<BaseEvent>> eventCapture = ArgumentCaptor.forClass(Supplier.class);
        verify(outboxService, times(1)).send(eventCapture.capture());
        var event = eventCapture.getValue().get();
        assertThat(event.getType()).isEqualTo("com.ritense.valtimo.task.unassigned");
        assertThat(event.getResultType()).isEqualTo("com.ritense.valtimo.camunda.domain.CamundaTask");
        assertThat(event.getResultId()).isEqualTo(task.getId());
        assertThat(event.getResult()).isNotNull();
    }

    @Test
    void taskComplete_withVariablesAndValidationException() {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("test", "test");

        when(camundaTaskRepository.findById(any())).thenReturn(Optional.of(
            task));
        doThrow(new FormFieldValidationException("a error")).when(formService).submitTaskForm(anyString(), anyMap());

        assertThrows(
            FormFieldValidationException.class,
            () -> camundaTaskService.completeTaskWithFormData(TASK_ID, variables)
        );
        verify(outboxService, times(0)).send(any());
    }

    @Test
    void taskComplete_withoutVariablesSuccessfully() {
        //initialize own taskService here because we need to override the complete method
        CamundaTaskService camundaTaskService = spy(new CamundaTaskService(
            taskService,
            null,
            delegateTaskHelper,
            camundaTaskRepository,
            null,
            Optional.empty(),
            applicationEventPublisher,
            null,
            userManagementService,
            entityManager,
            authorizationService,
            outboxService, objectMapper
        ));

        when(camundaTaskRepository.findById(any())).thenReturn(Optional.of(
            task));
        doNothing().when(taskService).complete(TASK_ID);

        camundaTaskService.completeTaskWithFormData(TASK_ID, null);
        verify(camundaTaskService, times(1)).completeTaskWithFormData(TASK_ID, null);
        verify(taskService, times(1)).complete(TASK_ID);

        ArgumentCaptor<Supplier<BaseEvent>> eventCapture = ArgumentCaptor.forClass(Supplier.class);
        verify(outboxService, times(1)).send(eventCapture.capture());
        var event = eventCapture.getValue().get();
        assertThat(event.getType()).isEqualTo("com.ritense.valtimo.task.completed");
        assertThat(event.getResultType()).isEqualTo("com.ritense.valtimo.camunda.domain.CamundaTask");
        assertThat(event.getResultId()).isEqualTo(task.getId());
        assertThat(event.getResult()).isNotNull();
    }

    @Test
    void taskComplete_withoutVariablesAndNoAuthorization() {
        when(camundaTaskRepository.findById(any())).thenReturn(Optional.of(
            task));
        doThrow(new AuthorizationException("some reason")).when(taskService).complete(anyString());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.completeTaskWithFormData(TASK_ID, null));
        verify(outboxService, times(0)).send(any());
    }

    @Test
    void taskComplete_withoutVariablesAndProcessEngineException() {
        when(camundaTaskRepository.findById(any())).thenReturn(Optional.of(
            task));
        doThrow(new ProcessEngineException()).when(taskService).complete(anyString());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.completeTaskWithFormData(TASK_ID, null));
        verify(outboxService, times(0)).send(any());
    }

}