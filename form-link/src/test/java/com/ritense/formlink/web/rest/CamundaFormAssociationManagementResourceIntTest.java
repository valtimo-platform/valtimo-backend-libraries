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

package com.ritense.formlink.web.rest;

import com.jayway.jsonpath.JsonPath;
import com.ritense.form.domain.FormDefinition;
import com.ritense.form.service.impl.FormIoFormDefinitionService;
import com.ritense.formlink.BaseIntegrationTest;
import com.ritense.formlink.repository.ProcessFormAssociationRepository;
import com.ritense.formlink.service.impl.CamundaFormAssociationService;
import com.ritense.valtimo.contract.utils.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
public class CamundaFormAssociationManagementResourceIntTest extends BaseIntegrationTest {

    @Inject
    public ProcessFormAssociationRepository processFormAssociationRepository;

    @Inject
    private FormAssociationManagementResource resource;

    @Inject
    public CamundaFormAssociationService formAssociationService;

    @Inject
    public FormIoFormDefinitionService formDefinitionService;

    private FormDefinition formDefinition;
    private MockMvc mockMvc;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("DELETE FROM process_form_association_v2");
        jdbcTemplate.execute("DELETE FROM form_io_form_definition");
        mockMvc = MockMvcBuilders
            .standaloneSetup(resource)
            .alwaysDo(print())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldReturn200WithFormAssociations() throws Exception {
        processFormAssociationRepository.add(
            PROCESS_DEFINITION_KEY,
            processFormAssociation(UUID.randomUUID(), UUID.randomUUID()).getFormAssociations().stream().findFirst().orElseThrow()
        );

        mockMvc.perform(
                get("/api/form-association-management").param("processDefinitionKey", PROCESS_DEFINITION_KEY)
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("*").isArray())
            .andExpect(jsonPath("*", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldReturn200WithFormAssociationCreated() throws Exception {
        final var createFormDefinitionRequest = createFormDefinitionRequest();
        formDefinition = formDefinitionService.createFormDefinition(createFormDefinitionRequest);

        final var request = createUserTaskFormAssociationRequest(formDefinition.getId());

        mockMvc.perform(
                post("/api/form-association-management")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .content(TestUtil.convertObjectToJsonBytes(request))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        final var formAssociation = formAssociationService
            .getFormAssociationByFormLinkId(PROCESS_DEFINITION_KEY, request.getFormLinkRequest().getId());
        assertThat(formAssociation).isPresent();
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldReturn200WithFormAssociationModified() throws Exception {
        final var createFormDefinitionRequest = createFormDefinitionRequest();
        formDefinition = formDefinitionService.createFormDefinition(createFormDefinitionRequest);
        var secondFormDefinition = formDefinitionService.createFormDefinition(createFormDefinitionRequest);

        final var request = createUserTaskFormAssociationRequest(formDefinition.getId());
        final MvcResult result = mockMvc.perform(
                post("/api/form-association-management")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .content(TestUtil.convertObjectToJsonBytes(request))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        final var documentContext = JsonPath.parse(result.getResponse().getContentAsString());
        final var id = UUID.fromString(documentContext.read("$['id']").toString());
        final var modifyFormLinkRequest = modifyFormAssociationRequest(id, secondFormDefinition.getId(), true);
        mockMvc.perform(
                put("/api/form-association-management")
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .content(TestUtil.convertObjectToJsonBytes(modifyFormLinkRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        final var formAssociation = formAssociationService
            .getFormAssociationByFormLinkId(PROCESS_DEFINITION_KEY, request.getFormLinkRequest().getId());
        assertThat(formAssociation).isPresent();
        assertThat(formAssociation.get().getFormLink().getFormId()).isEqualTo(secondFormDefinition.getId());
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = ADMIN)
    public void shouldReturn204WithFormAssociationDeleted() throws Exception {
        var processFormAssociationId = UUID.randomUUID();
        var formId = UUID.randomUUID();

        final var camundaProcessFormAssociation = processFormAssociation(
            processFormAssociationId,
            formId
        );

        final var formAssociation = camundaProcessFormAssociation
            .getFormAssociations()
            .stream()
            .findFirst()
            .orElseThrow();

        processFormAssociationRepository.add(
            camundaProcessFormAssociation.getProcessDefinitionKey(),
            formAssociation
        );

        mockMvc.perform(
                delete(
                    "/api/form-association-management/{processDefinitionKey}/{formAssociationId}",
                    PROCESS_DEFINITION_KEY,
                    formAssociation.getId()
                )
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNoContent());
    }

}
