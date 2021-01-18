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

import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.camunda.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class CamundaTaskServiceIntTest extends BaseIntegrationTest {

    @Inject
    private CamundaTaskService camundaTaskService;

    @Inject
    private CamundaProcessService camundaProcessService;

    private ProcessInstanceWithDefinition processInstanceWithDefinition;

    private final String processDefinitionKey = "one-task-process";
    private final String businessKey = "some-id";

    @BeforeEach
    public void setUp() throws IllegalAccessException {
        processInstanceWithDefinition = camundaProcessService.startProcess(
            processDefinitionKey,
            businessKey,
            Map.of()
        );
        addProcessToContext(processDefinitionKey);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    public void getProcessInstanceTasks() {
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
    public void shouldFindTasksFiltered() throws IllegalAccessException {
        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 5)
        );

        var task = pagedTasks.get().findFirst().orElseThrow();
        assertThat(pagedTasks.getTotalElements()).isEqualTo(1);
        assertThat(task.businessKey).isEqualTo(businessKey);
        assertThat(task.processDefinitionKey).isEqualTo(processDefinitionKey);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    public void shouldFind10TasksFiltered() throws IllegalAccessException {
        for (int i = 0; i < 9; i++) {
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
        assertThat(task.businessKey).isEqualTo(businessKey);
        assertThat(task.processDefinitionKey).isEqualTo(processDefinitionKey);
    }

    @Test
    @WithMockUser(username = "user@ritense.com", authorities = USER)
    public void shouldFindCandidateUsers() throws IllegalAccessException {
        var pagedTasks = camundaTaskService.findTasksFiltered(
            CamundaTaskService.TaskFilter.ALL,
            PageRequest.of(0, 20)
        );

        var task = pagedTasks.get().findFirst().orElseThrow().getId();

        //when(userManagementService.findByRole(eq("ROLE_USER"))).thenReturn(Collections.emptyList());
        List<ManageableUser> candidateUsers = camundaTaskService.getCandidateUsers(task);

        assertThat(candidateUsers).isEmpty();
    }
}