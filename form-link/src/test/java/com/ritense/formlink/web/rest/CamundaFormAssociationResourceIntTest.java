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

package com.ritense.formlink.web.rest;

import com.ritense.form.domain.FormDefinition;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseIntegrationTest;
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation;
import com.ritense.formlink.service.impl.CamundaFormAssociationService;
import com.ritense.formlink.web.rest.impl.CamundaFormAssociationResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
public class CamundaFormAssociationResourceIntTest extends BaseIntegrationTest {

    private MockMvc mockMvc;

    @Inject
    public CamundaFormAssociationResource formAssociationResource;

    @Inject
    public CamundaFormAssociationService formAssociationService;

    @Inject
    public FormIoFormDefinitionService formDefinitionService;

    private FormDefinition formDefinition;
    private CamundaFormAssociation userTaskFormAssociation;
    private CamundaFormAssociation startEventformAssociation;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() throws IOException {
        jdbcTemplate.execute("DELETE FROM process_form_association_v2");
        jdbcTemplate.execute("DELETE FROM form_io_form_definition");
        formDefinition = formDefinitionService.createFormDefinition(createFormDefinitionRequest());
        userTaskFormAssociation = formAssociationService.createFormAssociation(createUserTaskFormAssociationRequest(formDefinition.getId()));
        startEventformAssociation = formAssociationService.createFormAssociation(createFormAssociationRequestWithStartEvent(formDefinition.getId()));
        mockMvc = MockMvcBuilders.standaloneSetup(formAssociationResource).build();
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldReturn200WithFormDefinitionByFormLinkId() throws Exception {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("processDefinitionKey", Collections.singletonList(PROCESS_DEFINITION_KEY));
        parameters.put("formLinkId", Collections.singletonList(userTaskFormAssociation.getFormLink().getId()));

        mockMvc.perform(
            get("/api/v1/form-association/form-definition").params(parameters).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldReturn200WithFormDefinitionByFormKey() throws Exception {
        mockMvc.perform(
            get("/api/v1/form-association/form-definition/myForm").contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldReturn200WithStartEventFormDefinitionByProcessDefinitionKey() throws Exception {
        mockMvc.perform(
            get("/api/v1/form-association/form-definition")
                .param("processDefinitionKey", PROCESS_DEFINITION_KEY)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldHavePermissionWithNoTaskIdSubmission() throws Exception {

        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("processDefinitionKey", Collections.singletonList(PROCESS_DEFINITION_KEY));
        parameters.put("formLinkId", Collections.singletonList("formLinkId"));
        mockMvc.perform(
            post("/api/v1/form-association/form-definition/submission")
                .params(parameters)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

}
