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

package com.ritense.valtimo.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.service.request.AssigneeRequest;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskResourceTest {

    private MockMvc mockMvc;
    private TaskResource taskResource;
    private TaskService taskService;
    private FormService formService;
    private CamundaTaskService camundaTaskService;
    private CamundaProcessService camundaProcessService;
    private AssigneeRequest assigneeRequest;
    private ObjectMapper objectMapper;
    private String assigneeEmail = "A@A.com";
    private String taskId = UUID.randomUUID().toString();

    @BeforeEach
    void init() {
        taskService = mock(TaskService.class);
        formService = mock(FormService.class);
        camundaTaskService = mock(CamundaTaskService.class);
        camundaProcessService = mock(CamundaProcessService.class);

        taskResource = new TaskResource(
            taskService,
            formService,
            camundaTaskService,
            camundaProcessService
        );
        objectMapper = new ObjectMapper();

        assigneeRequest = new AssigneeRequest(assigneeEmail);

        mockMvc = MockMvcBuilders.standaloneSetup(taskResource)
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

        verify(camundaTaskService, times(1)).assign(eq(taskId), eq(assigneeEmail));
    }

}