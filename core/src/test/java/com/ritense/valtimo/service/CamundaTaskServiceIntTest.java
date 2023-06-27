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

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;

import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.camunda.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private final String processDefinitionKey = "one-task-process";
    private final String businessKey = "some-id";

    @BeforeEach
    void setUp() throws IllegalAccessException {
        addProcessToContext(processDefinitionKey);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    void getProcessInstanceTasks() {
        ProcessInstanceWithDefinition processInstanceWithDefinition = camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of()
        );

        final var processInstance = camundaProcessService
            .findProcessInstanceById(processInstanceWithDefinition.getProcessInstanceDto().getId()).orElseThrow();
        final var processInstanceTasks = camundaTaskService
            .getProcessInstanceTasks(processInstance.getId(), processInstance.getBusinessKey());

        final var task = processInstanceTasks.get(0);
        assertThat(task.getIdentityLinks()).isNotNull();
        assertThat(task.getBusinessKey()).isEqualTo(businessKey);
        assertThat(task.getProcessDefinitionKey()).isEqualTo(processDefinitionKey);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    void shouldFindTasksFiltered() throws IllegalAccessException {
        camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of()
        );

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
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    void shouldFindTasksFilteredWithContext() throws IllegalAccessException {
        camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of("context", "something")
        );

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
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    void shouldFind10TasksFiltered() throws IllegalAccessException {
        for (int i = 0; i < 10; i++) {
            camundaProcessService.startProcess(
                processDefinitionKey,
                businessKey,
                Map.of()
            );
        }

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
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    void shouldSortTasksByName() throws IllegalAccessException {
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
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    void shouldSortTasksByDueDate() throws IllegalAccessException {
        startProcessAndModifyTask(task1 -> task1.setDueDate(Date.valueOf("2022-06-17")));
        startProcessAndModifyTask(task2 -> task2.setDueDate(Date.valueOf("2022-06-18")));

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 2, Sort.Direction.DESC, "due")
        );

        var tasks = pagedTasks.toList();
        assertThat(tasks.get(0).getDue().toInstant()).hasToString("2022-06-18T00:00:00Z");
        assertThat(tasks.get(1).getDue().toInstant()).hasToString("2022-06-17T00:00:00Z");
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    void shouldSortTasksByAssignee() throws IllegalAccessException {
        startProcessAndModifyTask(task1 -> task1.setAssignee("userA@ritense.com"));
        startProcessAndModifyTask(task2 -> task2.setAssignee("userB@ritense.com"));

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 2, Sort.Direction.DESC, "assignee")
        );

        var tasks = pagedTasks.toList();
        assertThat(tasks.get(0).getAssignee()).isEqualTo("userB@ritense.com");
        assertThat(tasks.get(1).getAssignee()).isEqualTo("userA@ritense.com");
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    void shouldFindCandidateUsers() throws IllegalAccessException {
        final var processInstance = camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of()
        );

        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 20)
        );

        var task = pagedTasks.get().findFirst().orElseThrow().getId();

        List<ManageableUser> candidateUsers = camundaTaskService.getCandidateUsers(task);

        assertThat(candidateUsers).isEmpty();
    }

    private void startProcessAndModifyTask(Consumer<Task> taskHandler) {
        final var processInstance = camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of()
        );

        final var task = taskService.createTaskQuery()
            .processInstanceId(processInstance.getProcessInstanceDto().getId())
            .singleResult();

        taskHandler.accept(task);
        taskService.saveTask(task);
    }
}
