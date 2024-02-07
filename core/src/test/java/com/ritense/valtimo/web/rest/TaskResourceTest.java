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

package com.ritense.valtimo.web.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.camunda.dto.TaskExtended;
import com.ritense.valtimo.contract.json.MapperSingleton;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.service.request.AssigneeRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.camunda.bpm.engine.FormService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TaskResourceTest {

    private MockMvc mockMvc;
    private TaskResource taskResource;
    private FormService formService;
    private CamundaTaskService camundaTaskService;
    private CamundaProcessService camundaProcessService;
    private AssigneeRequest assigneeRequest;
    private ObjectMapper objectMapper;
    private String assigneeId = "AAAA-1111";
    private String taskId = UUID.randomUUID().toString();

    @BeforeEach
    void init() {
        formService = mock(FormService.class);
        camundaTaskService = mock(CamundaTaskService.class);
        camundaProcessService = mock(CamundaProcessService.class);

        taskResource = new TaskResource(
            formService,
            camundaTaskService,
            camundaProcessService
        );
        objectMapper = MapperSingleton.INSTANCE.get();

        assigneeRequest = new AssigneeRequest(assigneeId);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(taskResource)
            .setMessageConverters(converter)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void assign() throws Exception {
        mockMvc.perform(post("/api/v1/task/{taskId}/assign", taskId)
                .content(objectMapper.writeValueAsString(assigneeRequest))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(print())
            .andExpect(status().isOk());

        verify(camundaTaskService, times(1)).assign(taskId, assigneeId);
    }

    @Test
    void getTasksPaged() throws Exception {
        List<TaskExtended> tasks = List.of(
            new TaskExtended(
                "1",
                "name",
                "assignee",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                "delegationState",
                "description",
                "executionId",
                "owner",
                "parentTaskId",
                1,
                "processDefinitionId",
                "processInstanceId",
                "taskDefinitionKey",
                "caseExecutionId",
                "caseInstanceId",
                "caseDefinitionId",
                true,
                "tenantId",
                "businessKey",
                "processDefinitionKey",
                null,
                null
            )
        );

        Pageable pageable = PageRequest.of(1, 1);

        when(camundaTaskService.findTasksFiltered(any(), any())).thenReturn(new PageImpl<>(tasks, pageable, 5L));

        mockMvc.perform(get("/api/v2/task?filter=all")
                .content(objectMapper.writeValueAsString(assigneeRequest))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(tasks.get(0).getId()))
            .andExpect(jsonPath("$.content[0].name").value(tasks.get(0).getName()))
            .andExpect(jsonPath("$.content[0].assignee").value(tasks.get(0).getAssignee()))
            .andExpect(jsonPath("$.content[0].created").isNotEmpty())
            .andExpect(jsonPath("$.content[0].due").isNotEmpty())
            .andExpect(jsonPath("$.content[0].followUp").isNotEmpty())
            .andExpect(jsonPath("$.content[0].delegationState").value(tasks.get(0).getDelegationState()))
            .andExpect(jsonPath("$.content[0].description").value(tasks.get(0).getDescription()))
            .andExpect(jsonPath("$.content[0].executionId").value(tasks.get(0).getExecutionId()))
            .andExpect(jsonPath("$.content[0].owner").value(tasks.get(0).getOwner()))
            .andExpect(jsonPath("$.content[0].parentTaskId").value(tasks.get(0).getParentTaskId()))
            .andExpect(jsonPath("$.content[0].priority").value(tasks.get(0).getPriority()))
            .andExpect(jsonPath("$.content[0].processDefinitionId").value(tasks.get(0).getProcessDefinitionId()))
            .andExpect(jsonPath("$.content[0].processInstanceId").value(tasks.get(0).getProcessInstanceId()))
            .andExpect(jsonPath("$.content[0].taskDefinitionKey").value(tasks.get(0).getTaskDefinitionKey()))
            .andExpect(jsonPath("$.content[0].caseExecutionId").value(tasks.get(0).getCaseExecutionId()))
            .andExpect(jsonPath("$.content[0].caseInstanceId").value(tasks.get(0).getCaseInstanceId()))
            .andExpect(jsonPath("$.content[0].caseDefinitionId").value(tasks.get(0).getCaseDefinitionId()))
            .andExpect(jsonPath("$.content[0].suspended").value(tasks.get(0).getSuspended()))
            .andExpect(jsonPath("$.content[0].tenantId").value(tasks.get(0).getTenantId()))
            .andExpect(jsonPath("$.content[0].businessKey").value(tasks.get(0).getBusinessKey()))
            .andExpect(jsonPath("$.content[0].processDefinitionKey").value(tasks.get(0).getProcessDefinitionKey()))
            .andExpect(jsonPath("$.totalElements").value(5))
            .andExpect(jsonPath("$.totalPages").value(5));
    }

}