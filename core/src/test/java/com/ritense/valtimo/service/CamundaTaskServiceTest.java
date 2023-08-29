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

package com.ritense.valtimo.service;

import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.specification.AuthorizationSpecification;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.repository.CamundaTaskRepository;
import com.ritense.valtimo.camunda.service.CamundaContextService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.security.exceptions.TaskNotFoundException;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CamundaTaskServiceTest {
    private static final String TASK_ID = "task";
    private static final String ASSIGNEE = "theAssignee";
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
    private CamundaContextService camundaContextService;

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
        camundaContextService = mock(CamundaContextService.class);
        task = new CamundaTask(TASK_ID, 0, null, null, null, List.of(), null, null, null, null, null, null, null, null, null, null, 0, null, null, null, null, 0, null, Set.of());
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
                camundaContextService)
        );
        when(authorizationService.getAuthorizationSpecification(any(), any()))
            .thenReturn(mock(AuthorizationSpecification.class));
    }

    @Test
    void findTaskById_taskDoesNotExists() {
        assertThrows(TaskNotFoundException.class, () -> camundaTaskService.findTaskById(TASK_ID));
    }

    @Test
    void claimTask_taskDoesNotExist() {
        when(camundaTaskRepository.findOne(ArgumentMatchers.<Specification<CamundaTask>>any())).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> camundaTaskService.findTaskById(TASK_ID));
    }

    @Test
    void claimTask_taskClaimedDoesntExist() {
        //when
        when(camundaTaskRepository.findOne(ArgumentMatchers.<Specification<CamundaTask>>any())).thenReturn(Optional.of(task));
        doThrow(new ProcessEngineException()).when(taskService).setAssignee(eq(TASK_ID), eq(ASSIGNEE));
        doReturn(task).when(camundaTaskService).findTaskById(TASK_ID);

        assertThrows(IllegalStateException.class, () -> camundaTaskService.assign(TASK_ID, ASSIGNEE));
    }

    @Test
    void claimTask_taskClaimedWithNoPermissions() {
        when(camundaTaskRepository.findOne(ArgumentMatchers.<Specification<CamundaTask>>any())).thenReturn(Optional.of(task));
        doThrow(new AuthorizationException("some reason")).when(taskService).setAssignee(eq(TASK_ID), eq(ASSIGNEE));
        assertThrows(IllegalStateException.class, () -> camundaTaskService.assign(TASK_ID, ASSIGNEE));
    }

    @Test
    void unclaimTask_taskDoesNotExist() {
        doThrow(new IllegalStateException()).when(camundaTaskService).findTaskById(TASK_ID);
        assertThrows(IllegalStateException.class, () -> camundaTaskService.unassign(TASK_ID));
    }

    @Test
    void unclaimTask_taskUnclaimedDoesntExist() {
        when(camundaTaskRepository.findOne(ArgumentMatchers.<Specification<CamundaTask>>any())).thenReturn(Optional.of(task));
        doThrow(new ProcessEngineException()).when(taskService).setAssignee(eq(TASK_ID), isNull());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.unassign(TASK_ID));
    }

    @Test
    void unclaimTask_taskClaimedWithNoPermissions() {
        when(camundaTaskRepository.findOne(ArgumentMatchers.<Specification<CamundaTask>>any())).thenReturn(Optional.of(task));
        doThrow(new AuthorizationException("some reason")).when(taskService).setAssignee(eq(TASK_ID), isNull());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.unassign(TASK_ID));
    }

    @Test
    void taskComplete_withVariablesAndValidationException() {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("test", "test");

        when(camundaTaskRepository.findOne(ArgumentMatchers.<Specification<CamundaTask>>any())).thenReturn(Optional.of(task));
        doThrow(new FormFieldValidationException("a error")).when(formService).submitTaskForm(anyString(), anyMap());

        assertThrows(FormFieldValidationException.class, () -> camundaTaskService.completeTaskWithFormData(TASK_ID, variables));
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
            camundaContextService));

        when(camundaTaskRepository.findOne(ArgumentMatchers.<Specification<CamundaTask>>any())).thenReturn(Optional.of(task));
        doNothing().when(taskService).complete(TASK_ID);

        camundaTaskService.completeTaskWithFormData(TASK_ID, null);
        verify(camundaTaskService, times(1)).completeTaskWithFormData(TASK_ID, null);
        verify(taskService, times(1)).complete(TASK_ID);
    }

    @Test
    void taskComplete_withoutVariablesAndNoAuthorization() {
        when(camundaTaskRepository.findOne(ArgumentMatchers.<Specification<CamundaTask>>any())).thenReturn(Optional.of(task));
        doThrow(new AuthorizationException("some reason")).when(taskService).complete(anyString());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.completeTaskWithFormData(TASK_ID, null));
    }

    @Test
    void taskComplete_withoutVariablesAndProcessEngineException() {
        when(camundaTaskRepository.findOne(ArgumentMatchers.<Specification<CamundaTask>>any())).thenReturn(Optional.of(task));
        doThrow(new ProcessEngineException()).when(taskService).complete(anyString());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.completeTaskWithFormData(TASK_ID, null));
    }

}