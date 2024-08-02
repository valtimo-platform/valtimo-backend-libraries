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

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.permission.ConditionContainer;
import com.ritense.authorization.permission.Permission;
import com.ritense.authorization.permission.PermissionRepository;
import com.ritense.authorization.role.RoleRepository;
import com.ritense.outbox.repository.OutboxMessageRepository;
import com.ritense.outbox.domain.BaseEvent;
import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.camunda.authorization.CamundaTaskActionProvider;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.NamedUser;
import jakarta.inject.Inject;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CamundaTaskServiceIntTest extends BaseIntegrationTest {

    @Inject
    private CamundaTaskService camundaTaskService;

    @Inject
    private TaskService taskService;

    @Inject
    private CamundaProcessService camundaProcessService;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private PermissionRepository permissionRepository;

    @Inject
    private OutboxMessageRepository outboxRepository;

    private final String processDefinitionKey = "one-task-process";
    private final String businessKey = "some-id";

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void getProcessInstanceTasks() {
        ProcessInstanceWithDefinition processInstanceWithDefinition = runWithoutAuthorization(() -> camundaProcessService.startProcess(
                processDefinitionKey,
                businessKey,
                Map.of()
            ));

        final var processInstance = runWithoutAuthorization(
                () -> camundaProcessService
                    .findProcessInstanceById(processInstanceWithDefinition.getProcessInstanceDto().getId()).orElseThrow());
        final var processInstanceTasks = camundaTaskService
            .getProcessInstanceTasks(processInstance.getId(), processInstance.getBusinessKey());

        final var task = processInstanceTasks.get(0);
        assertThat(task.getIdentityLinks()).isNotNull();
        assertThat(task.getBusinessKey()).isEqualTo(businessKey);
        assertThat(task.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldFindTasksFiltered() throws IllegalAccessException {
        runWithoutAuthorization(() -> camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of()
        ));

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 5)
        );

        var task = pagedTasks.get().findFirst().orElseThrow();
        assertThat(pagedTasks.getTotalElements()).isEqualTo(1);
        assertThat(task.getBusinessKey()).isEqualTo(businessKey);
        assertThat(task.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(task.getContext()).isNull();
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldFindAssignedTasksByUserIdentifier() {
        ManageableUser user = mock();
        when(user.getUserIdentifier()).thenReturn("user+1@ritense.com");
        when(userManagementService.getCurrentUser()).thenReturn(user);
        startProcessAndModifyTask(task1 -> task1.setAssignee(user.getUserIdentifier()));

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.MINE,
            PageRequest.of(0, 5)
        );

        assertThat(pagedTasks.getTotalElements()).isEqualTo(1);
        var task = pagedTasks.get().findFirst().orElseThrow();
        assertThat(task.getAssignee()).isEqualTo(user.getUserIdentifier());
        assertThat(task.getBusinessKey()).isEqualTo(businessKey);
        assertThat(task.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(task.getContext()).isNull();
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldFindTasksFilteredWithContext() {
        runWithoutAuthorization(() -> camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of("context", "something")
        ));

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 5)
        );

        var task = pagedTasks.get().findFirst().orElseThrow();
        assertThat(pagedTasks.getTotalElements()).isEqualTo(1);
        assertThat(task.getBusinessKey()).isEqualTo(businessKey);
        assertThat(task.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
        assertThat(task.getContext()).isEqualTo("something");
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldFind10TasksFiltered() throws IllegalAccessException {
        runWithoutAuthorization(() -> {
            for (int i = 0; i < 10; i++) {
                camundaProcessService.startProcess(
                    processDefinitionKey,
                    businessKey,
                    Map.of()
                );
            }
            return null;
        });

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 20)
        );

        var task = pagedTasks.get().findFirst().orElseThrow();
        assertThat(pagedTasks.getTotalElements()).isEqualTo(10);
        assertThat(task.getBusinessKey()).isEqualTo(businessKey);
        assertThat(task.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = "IDENTITY_LINK_ROLE")
    void shouldGetCorrectTotalAmountWhenMoreThanPageSize() {
        AuthorizationContext.runWithoutAuthorization(() -> {
            for (int i = 0; i < 10; i++) {
                camundaProcessService.startProcess(
                    "identity-link-mapper-test-process",
                    businessKey,
                    Map.of()
                );
            }
            return null;
        });

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 5)
        );

        assertThat(pagedTasks.getTotalElements()).isEqualTo(10);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldSortTasksByName() {
        startProcessAndModifyTask(task1 -> task1.setName("B"));
        startProcessAndModifyTask(task2 -> task2.setName("A"));

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 2, Sort.Direction.ASC, "name")
        );

        var tasks = pagedTasks.toList();
        assertThat(tasks.get(0).getName()).isEqualTo("A");
        assertThat(tasks.get(1).getName()).isEqualTo("B");
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldSortTasksByDueDate() {
        startProcessAndModifyTask(task1 -> task1.setDueDate(Date.valueOf("2022-06-17")));
        startProcessAndModifyTask(task2 -> task2.setDueDate(Date.valueOf("2022-06-18")));

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 2, Sort.Direction.DESC, "due")
        );

        var tasks = pagedTasks.toList();
        assertThat(tasks.get(0).getDue()).hasToString("2022-06-18T00:00");
        assertThat(tasks.get(1).getDue()).hasToString("2022-06-17T00:00");
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldSortTasksByAssignee() {
        startProcessAndModifyTask(task1 -> task1.setAssignee("AAAA-1111"));
        startProcessAndModifyTask(task2 -> task2.setAssignee("BBBB-2222"));

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 2, Sort.Direction.DESC, "assignee")
        );

        var tasks = pagedTasks.toList();
        assertThat(tasks.get(0).getAssignee()).isEqualTo("BBBB-2222");
        assertThat(tasks.get(1).getAssignee()).isEqualTo("AAAA-1111");
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldFindCandidateUsers() {
        var adminRole = roleRepository.findByKey(ADMIN);
        var userRole = roleRepository.findByKey(USER);
        var permissions = List.of(
            new Permission(
                UUID.randomUUID(),
                CamundaTask.class,
                CamundaTaskActionProvider.ASSIGN,
                new ConditionContainer(),
                adminRole
            ),
            new Permission(
                UUID.randomUUID(),
                CamundaTask.class,
                CamundaTaskActionProvider.ASSIGNABLE,
                new ConditionContainer(),
                userRole
            )
        );

        permissionRepository.deleteAll();
        permissionRepository.saveAllAndFlush(permissions);

        ManageableUser manageableUser = mock(ManageableUser.class);
        when(userManagementService.findByRole(userRole.getKey())).thenReturn(List.of(manageableUser));

        runWithoutAuthorization(() -> camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of()
        ));


        var pagedTasks = runWithoutAuthorization(() -> camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 20)
        ));

        var task = pagedTasks.get().findFirst().orElseThrow().getId();

        List<ManageableUser> candidateUsers = camundaTaskService.getCandidateUsers(task);

        assertThat(candidateUsers).contains(manageableUser);
        verify(userManagementService, never()).findByRole(ADMIN);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldUnassignTask() {
        var adminRole = roleRepository.findByKey(ADMIN);
        var userRole = roleRepository.findByKey(USER);
        var permissions = List.of(
            new Permission(
                UUID.randomUUID(),
                CamundaTask.class,
                CamundaTaskActionProvider.ASSIGN,
                new ConditionContainer(),
                adminRole
            ),
            new Permission(
                UUID.randomUUID(),
                CamundaTask.class,
                CamundaTaskActionProvider.ASSIGNABLE,
                new ConditionContainer(),
                userRole
            )
        );

        permissionRepository.deleteAll();
        permissionRepository.saveAllAndFlush(permissions);

        ManageableUser manageableUser = mock(ManageableUser.class);
        when(userManagementService.findByRole(userRole.getKey())).thenReturn(List.of(manageableUser));

        runWithoutAuthorization(() -> camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of()
        ));


        var pagedTasks = runWithoutAuthorization(() -> camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 20)
        ));

        var task = pagedTasks.get().findFirst().orElseThrow().getId();

        camundaTaskService.assign(task, null);

        assertThat(outboxRepository.count()).isEqualTo(1);

        ArgumentCaptor<Supplier<BaseEvent>> eventCapture = ArgumentCaptor.forClass(Supplier.class);
        verify(outboxService, times(1)).send(eventCapture.capture());
        var event = eventCapture.getValue().get();
        assertThat(event.getType()).isEqualTo("com.ritense.valtimo.task.unassigned");
        assertThat(event.getResultType()).isEqualTo("com.ritense.valtimo.camunda.domain.CamundaTask");
        assertThat(event.getResultId()).isEqualTo(task);
        assertThat(event.getResult()).isNotNull();
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldGetSerializedVariable() {
        final var processInstance = runWithoutAuthorization(() -> camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of("serialized_var", LocalDateTime.now())
        ));
        final var task = taskService.createTaskQuery()
            .processInstanceId(processInstance.getProcessInstanceDto().getId())
            .singleResult();

        var variables = camundaTaskService.getVariables(task.getId());

        assertThat(variables.get("serialized_var")).isNotNull();
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldFindNamedCandidateUsers() {
        var user = new NamedUser("id", "user@ritense.com", "John", "Doe");
        when(userManagementService.findNamedUserByRoles(Set.of(ADMIN))).thenReturn(List.of(user));
        runWithoutAuthorization(() -> camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of()
        ));
        var taskId = runWithoutAuthorization(() -> camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 20)
        )).get().findFirst().orElseThrow().getId();

        var candidateUsers = camundaTaskService.getNamedCandidateUsers(taskId);

        assertThat(candidateUsers).containsExactly(user);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = ADMIN)
    void shouldSendOutboxEventWhenTaskCompleted() {
        final var processInstance = runWithoutAuthorization(() ->
            camundaProcessService.startProcess(
                processDefinitionKey,
                businessKey,
                Map.of()
            ));
        final var task = taskService.createTaskQuery()
            .processInstanceId(processInstance.getProcessInstanceDto().getId())
            .singleResult();

        camundaTaskService.complete(task.getId());

        ArgumentCaptor<Supplier<BaseEvent>> eventCapture = ArgumentCaptor.forClass(Supplier.class);
        verify(outboxService, times(1)).send(eventCapture.capture());
        var event = eventCapture.getValue().get();
        assertThat(event.getType()).isEqualTo("com.ritense.valtimo.task.completed");
        assertThat(event.getResultType()).isEqualTo("com.ritense.valtimo.camunda.domain.CamundaTask");
        assertThat(event.getResultId()).isEqualTo(task.getId());
        assertThat(event.getResult()).isNotNull();
    }

    private void startProcessAndModifyTask(Consumer<Task> taskHandler) {
        final var processInstance = runWithoutAuthorization(() -> camundaProcessService.startProcess(
                processDefinitionKey,
                businessKey,
                Map.of()
            ));

        final var task = taskService.createTaskQuery()
            .processInstanceId(processInstance.getProcessInstanceDto().getId())
            .singleResult();

        taskHandler.accept(task);
        taskService.saveTask(task);
    }
}
