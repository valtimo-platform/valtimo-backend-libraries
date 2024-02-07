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
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.repository.camunda.dto.ProcessInstance;
import com.ritense.valtimo.web.rest.dto.ProcessInstanceSearchDTO;
import jakarta.inject.Inject;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProcessResourceIntTest extends BaseIntegrationTest {

    @Inject
    private ProcessResource processResource;

    @Inject
    public RepositoryService repositoryService;

    @Inject
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(processResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void shouldGetProcessDefinitions() throws Exception {
        mockMvc.perform(get("/api/v1/process/definition")
                .accept(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[?(@.key=='identity-link-mapper-test-process')].key").value("identity-link-mapper-test-process"))
            .andExpect(jsonPath("$.[?(@.key=='identity-link-mapper-test-process')].readOnly").value(false))
            .andExpect(jsonPath("$.[?(@.key=='one-task-process')].key").value("one-task-process"))
            .andExpect(jsonPath("$.[?(@.key=='one-task-process')].readOnly").value(false));
    }

    @Test
    void shouldGetProcessDefinitionXml() throws Exception {
        var processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("test-process")
            .latestVersion()
            .singleResult();

        mockMvc.perform(get("/api/v1/process/definition/{processDefinitionId}/xml", processDefinition.getId())
                .accept(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(processDefinition.getId()))
            .andExpect(jsonPath("$.bpmn20Xml").isNotEmpty())
            .andExpect(jsonPath("$.readOnly").value(true))
            .andExpect(jsonPath("$.systemProcess").value(true));
    }

    @Test
    void shouldThrowForbiddenWhenMigratingAReadOnlyProcess() throws Exception {
        var processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("test-process")
            .latestVersion()
            .singleResult();

        mockMvc.perform(
            post("/api/v1/process/definition/{sourceProcessDefinitionId}/{targetProcessDefinitionId}/migrate",
                processDefinition.getId(), processDefinition.getId())
                .accept(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetProcessInstances() throws Exception {

        List<ProcessInstance> processInstances = List.of(
            new ProcessInstance(
                "1",
                "businessKey",
                new Date(),
                null,
                "processDefinitionKey",
                "startUserId",
                "deleteReason"
            ),
            new ProcessInstance(
                "2",
                "businessKey",
                new Date(),
                null,
                "processDefinitionKey",
                "startUserId",
                "deleteReason"
            )
        );

        Pageable pageable = PageRequest.of(1, 1);

        doReturn(new PageImpl<>(processInstances, pageable, 5)).when(camundaSearchProcessInstanceRepository).searchInstances(any(), any(), any());

        mockMvc.perform(post("/api/v2/process/test-process/search")
            .content(objectMapper.writeValueAsString(new ProcessInstanceSearchDTO()))
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value("1"))
            .andExpect(jsonPath("$.content[0].businessKey").value("businessKey"))
            .andExpect(jsonPath("$.content[0].startTime").isNotEmpty())
            .andExpect(jsonPath("$.content[0].endTime").isEmpty())
            .andExpect(jsonPath("$.content[0].processDefinitionKey").value("processDefinitionKey"))
            .andExpect(jsonPath("$.content[0].startUserId").value("startUserId"))
            .andExpect(jsonPath("$.content[0].deleteReason").value("deleteReason"))
            .andExpect(jsonPath("$.totalElements").value(5))
            .andExpect(jsonPath("$.totalElements").value(5));
    }
}
