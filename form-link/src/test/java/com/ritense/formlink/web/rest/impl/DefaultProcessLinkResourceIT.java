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

package com.ritense.formlink.web.rest.impl;

import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.form.domain.FormDefinition;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseIntegrationTest;
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation;
import com.ritense.formlink.service.impl.CamundaFormAssociationService;
import com.ritense.formlink.web.rest.ProcessLinkResource;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DefaultProcessLinkResourceIT extends BaseIntegrationTest {

    private final static String DOCUMENT_DEFINITION_NAME = "house";

    @Inject
    public ProcessLinkResource processLinkResource;

    @Inject
    public CamundaFormAssociationService formAssociationService;

    @Inject
    public FormIoFormDefinitionService formDefinitionService;

    @Inject
    private ProcessDocumentService processDocumentService;

    @Inject
    private ProcessDocumentAssociationService pdaService;

    @Inject
    private TaskService taskService;

    private MockMvc mockMvc;
    private FormDefinition formDefinition;
    private CamundaFormAssociation userTaskFormAssociation;

    @BeforeEach
    public void setUp() throws IOException {
        formDefinition = formDefinitionService.createFormDefinition(createFormDefinitionRequest());
        userTaskFormAssociation = formAssociationService.createFormAssociation(createUserTaskFormAssociationRequest(formDefinition.getId()));
        pdaService.createProcessDocumentDefinition(new ProcessDocumentDefinitionRequest(
            PROCESS_DEFINITION_KEY,
            DOCUMENT_DEFINITION_NAME,
            true
        ));
        mockMvc = MockMvcBuilders.standaloneSetup(processLinkResource).build();
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    void getTaskShouldGetFormResult() throws Exception {
        var content = new JsonDocumentContent("{\"street\": \"Kalverstraat\"}");

        NewDocumentAndStartProcessResult result = processDocumentService.newDocumentAndStartProcess(new NewDocumentAndStartProcessRequest(
            PROCESS_DEFINITION_KEY,
            new NewDocumentRequest(
                "house",
                content.asJson()
            )
        ));

        List<Task> list = taskService.createTaskQuery().list();

        assertEquals(1, list.size());

        mockMvc.perform(
            get("/api/v1/process-link/task/" + list.get(0).getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.type").value("form"))
            .andExpect(jsonPath("$.properties.formLinkId").value("userTaskId"));
    }
}