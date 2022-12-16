/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import com.ritense.valtimo.security.exceptions.TaskNotFoundException;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import java.util.HashMap;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private Task task;
    private TaskService taskService;
    private FormService formService;
    private DelegateTaskHelper delegateTaskHelper;
    private ApplicationEventPublisher applicationEventPublisher;
    private CamundaTaskService camundaTaskService;
    private CamundaProcessService camundaProcessService;
    private RuntimeService runtimeService;
    private UserManagementService userManagementService;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class, RETURNS_DEEP_STUBS);
        formService = mock(FormService.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        runtimeService = mock(RuntimeService.class);
        delegateTaskHelper = mock(DelegateTaskHelper.class);
        camundaProcessService = mock(CamundaProcessService.class);
        userManagementService = mock(UserManagementService.class);
        task = new TaskEntity(TASK_ID);
        camundaTaskService = spy(
            new CamundaTaskService(
                taskService,
                formService,
                null,
                delegateTaskHelper,
                null,
                camundaProcessService,
                Optional.empty(),
                applicationEventPublisher,
                runtimeService,
                userManagementService
            )
        );
    }

    @Test
    void findTaskById_taskDoesNotExists() {
        when(taskService.createTaskQuery().taskId(TASK_ID).initializeFormKeys().singleResult()).thenReturn(null);
        assertThrows(TaskNotFoundException.class, () -> camundaTaskService.findTaskById(TASK_ID));
    }

    @Test
    void claimTask_taskDoesNotExist() {
        when(taskService.createTaskQuery().taskId(anyString()).initializeFormKeys().singleResult()).thenThrow(new ProcessEngineException());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.findTaskById(TASK_ID));
    }

    @Test
    void claimTask_taskClaimedDoesntExist() {
        //when
        when(taskService.createTaskQuery().taskId(TASK_ID).initializeFormKeys().singleResult()).thenReturn(task);
        doThrow(new ProcessEngineException()).when(taskService).setAssignee(eq(TASK_ID), eq(ASSIGNEE));
        doReturn(task).when(camundaTaskService).findTaskById(TASK_ID);

        assertThrows(IllegalStateException.class, () -> camundaTaskService.assign(TASK_ID, ASSIGNEE));
    }

    @Test
    void claimTask_taskClaimedWithNoPermissions() {
        when(taskService.createTaskQuery().taskId(TASK_ID).initializeFormKeys().singleResult()).thenReturn(task);
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
        when(taskService.createTaskQuery().taskId(TASK_ID).initializeFormKeys().singleResult()).thenReturn(task);
        doThrow(new ProcessEngineException()).when(taskService).setAssignee(eq(TASK_ID), isNull());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.unassign(TASK_ID));
    }

    @Test
    void unclaimTask_taskClaimedWithNoPermissions() {
        when(taskService.createTaskQuery().taskId(TASK_ID).initializeFormKeys().singleResult()).thenReturn(task);
        doThrow(new AuthorizationException("some reason")).when(taskService).setAssignee(eq(TASK_ID), isNull());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.unassign(TASK_ID));
    }

    @Test
    void taskComplete_withVariablesAndValidationException() {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("test", "test");

        when(taskService.createTaskQuery().taskId(TASK_ID).initializeFormKeys().singleResult()).thenReturn(task);
        doThrow(new FormFieldValidationException("a error")).when(formService).submitTaskForm(anyString(), anyMap());

        assertThrows(FormFieldValidationException.class, () -> camundaTaskService.completeTask(TASK_ID, variables));
    }

    @Test
    void taskComplete_withoutVariablesSuccessfully() {
        //initialize own taskService here because we need to override the complete method
        TaskService taskService = mock(TaskService.class, RETURNS_DEEP_STUBS);
        CamundaTaskService camundaTaskService = spy(new CamundaTaskService(taskService, null, null, delegateTaskHelper, null, null,
            Optional.empty(), applicationEventPublisher, null, userManagementService));

        when(taskService.createTaskQuery().taskId(TASK_ID).initializeFormKeys().singleResult()).thenReturn(task);
        doNothing().when(taskService).complete(TASK_ID);

        camundaTaskService.completeTask(TASK_ID, null);
        verify(camundaTaskService, times(1)).completeTask(TASK_ID, null);
        verify(taskService, times(1)).complete(TASK_ID);
    }

    @Test
    void taskComplete_withoutVariablesAndNoAuthorization() {
        when(taskService.createTaskQuery().taskId(TASK_ID).initializeFormKeys().singleResult()).thenReturn(task);
        doThrow(new AuthorizationException("some reason")).when(taskService).complete(anyString());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.completeTask(TASK_ID, null));
    }

    @Test
    void taskComplete_withoutVariablesAndProcessEngineException() {
        when(taskService.createTaskQuery().taskId(TASK_ID).initializeFormKeys().singleResult()).thenReturn(task);
        doThrow(new ProcessEngineException()).when(taskService).complete(anyString());
        assertThrows(IllegalStateException.class, () -> camundaTaskService.completeTask(TASK_ID, null));
    }
}