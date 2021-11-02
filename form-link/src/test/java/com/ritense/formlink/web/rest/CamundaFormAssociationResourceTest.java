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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.formlink.BaseTest;
import com.ritense.formlink.domain.impl.formassociation.CamundaProcessFormAssociation;
import com.ritense.formlink.service.impl.CamundaFormAssociationService;
import com.ritense.formlink.service.impl.CamundaFormAssociationSubmissionService;
import com.ritense.formlink.web.rest.impl.CamundaFormAssociationResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class CamundaFormAssociationResourceTest extends BaseTest {

    private CamundaFormAssociationResource camundaFormAssociationResource;
    private CamundaFormAssociationService camundaFormAssociationService;
    private MockMvc mockMvc;

    private CamundaProcessFormAssociation camundaProcessFormAssociation;

    @BeforeEach
    public void setUp() {
        camundaFormAssociationService = mock(CamundaFormAssociationService.class);
        var formAssociationSubmissionService = mock(CamundaFormAssociationSubmissionService.class);
        camundaFormAssociationResource = new CamundaFormAssociationResource(camundaFormAssociationService, formAssociationSubmissionService);
        mockMvc = MockMvcBuilders.standaloneSetup(camundaFormAssociationResource).build();
        camundaProcessFormAssociation = processFormAssociation(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldReturn200WithFormDefinitionByFormLinkId() throws Exception {
        final var formAssociation = camundaProcessFormAssociation.getFormAssociations().iterator().next();

        final ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        when(camundaFormAssociationService
            .getPreFilledFormDefinitionByFormLinkId(
                eq(camundaProcessFormAssociation.getProcessDefinitionKey()),
                eq(formAssociation.getFormLink().getId()),
                eq(Optional.empty()),
                eq(Optional.empty())
            )
        ).thenReturn(Optional.of(jsonNode));

        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("processDefinitionKey", Collections.singletonList(camundaProcessFormAssociation.getProcessDefinitionKey()));
        parameters.put("formLinkId", Collections.singletonList(formAssociation.getFormLink().getId()));

        mockMvc.perform(
            get("/api/form-association/form-definition").params(parameters).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    @Disabled//TODO find out why debugging doesnot work not the issue cannot be found as a result.
    public void shouldReturn200WithPreFilledFormDefinitionByFormLinkId() throws Exception {
        final var formAssociation = camundaProcessFormAssociation.getFormAssociations().iterator().next();
        final var documentId = JsonSchemaDocumentId.existingId(UUID.randomUUID());
        final ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        when(camundaFormAssociationService
            .getPreFilledFormDefinitionByFormLinkId(
                eq(documentId),
                eq(camundaProcessFormAssociation.getProcessDefinitionKey()),
                eq(formAssociation.getFormLink().getId())
            )
        ).thenReturn(Optional.of(jsonNode));

        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("documentId", Collections.singletonList(documentId.toString()));
        parameters.put("processDefinitionKey", Collections.singletonList(camundaProcessFormAssociation.getProcessDefinitionKey()));
        parameters.put("formLinkId", Collections.singletonList(formAssociation.getFormLink().getId()));
        //parameters.put("taskInstanceId", Collections.singletonList(null));

        mockMvc.perform(
            get("/api/form-association/form-definition").params(parameters)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john@ritense.com", authorities = USER)
    public void shouldReturn200WithStartFormByProcessDefinitionKey() throws Exception {
        final ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        when(camundaFormAssociationService
            .getStartEventFormDefinition(
                eq(camundaProcessFormAssociation.getProcessDefinitionKey())
            )
        ).thenReturn(Optional.of(jsonNode));

        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.put("processDefinitionKey", Collections.singletonList(camundaProcessFormAssociation.getProcessDefinitionKey()));

        mockMvc.perform(
            get("/api/form-association/form-definition").params(parameters).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());
    }

}
