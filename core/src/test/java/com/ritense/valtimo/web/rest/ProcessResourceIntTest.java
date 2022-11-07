/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import com.ritense.valtimo.BaseIntegrationTest;
import org.camunda.bpm.engine.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProcessResourceIntTest extends BaseIntegrationTest {

    @Inject
    private ProcessResource processResource;

    @Inject
    public RepositoryService repositoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(processResource).build();
    }

    @Test
    void shouldGetProcessDefinitions() throws Exception {
        mockMvc.perform(get("/api/v1/process/definition")
                .accept(APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].key").value("one-task-process"))
            .andExpect(jsonPath("$.[0].readOnly").value(false))
            .andExpect(jsonPath("$.[1].key").value("test-process"))
            .andExpect(jsonPath("$.[1].readOnly").value(true));
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

}
