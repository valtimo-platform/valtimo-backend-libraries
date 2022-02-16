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

package com.ritense.document.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.document.BaseTest;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.service.UndeployDocumentDefinitionService;
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService;
import com.ritense.document.service.impl.UndeployJsonSchemaDocumentDefinitionService;
import com.ritense.document.service.request.DocumentDefinitionCreateRequest;
import com.ritense.document.service.result.DeployDocumentDefinitionResultFailed;
import com.ritense.document.service.result.DeployDocumentDefinitionResultSucceeded;
import com.ritense.document.service.result.UndeployDocumentDefinitionResultFailed;
import com.ritense.document.service.result.UndeployDocumentDefinitionResultSucceeded;
import com.ritense.document.web.rest.impl.JsonSchemaDocumentDefinitionResource;
import com.ritense.valtimo.contract.authentication.CurrentUserService;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JsonSchemaDocumentDefinitionResourceTest extends BaseTest {

    public static final String SOME_ROLE = "SOME_ROLE";
    private JsonSchemaDocumentDefinitionService documentDefinitionService;
    private DocumentDefinitionResource documentDefinitionResource;
    private UndeployDocumentDefinitionService undeployDocumentDefinitionService;
    private MockMvc mockMvc;
    private Page<JsonSchemaDocumentDefinition> definitionPage;
    private JsonSchemaDocumentDefinition definition;
    private CurrentUserService currentUserService;
    private ValtimoUser userWithRole;

    @BeforeEach
    public void setUp() {
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        undeployDocumentDefinitionService = mock(UndeployJsonSchemaDocumentDefinitionService.class);
        currentUserService = mock(CurrentUserService.class);

        documentDefinitionResource = new JsonSchemaDocumentDefinitionResource(
            documentDefinitionService,
            undeployDocumentDefinitionService,
            currentUserService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(documentDefinitionResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

        definition = definition();
        List<JsonSchemaDocumentDefinition> definitions = List.of(definition);
        Pageable unpaged = Pageable.unpaged();

        definitionPage = new PageImpl<>(definitions, unpaged, 1);

        userWithRole = new ValtimoUser();
        userWithRole.setRoles(List.of(SOME_ROLE));
    }

    @Test
    public void shouldReturnPagedRecordPage() throws Exception {
        when(documentDefinitionService.findAll(any())).thenReturn(definitionPage);

        mockMvc.perform(get("/api/document-definition"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void shouldReturnSingleDefinitionRecordByName() throws Exception {
        String definitionName = definition.getId().name();
        when(documentDefinitionService.findLatestByName(anyString())).thenReturn(Optional.of(definition));
        when(currentUserService.getCurrentUser()).thenReturn(userWithRole);
        when(documentDefinitionService.getDocumentDefinitionRoles(eq(definitionName))).thenReturn(Set.of(SOME_ROLE));
        mockMvc.perform(get("/api/document-definition/{name}", definitionName))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void shouldReturnCreateSuccessResult() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        DocumentDefinitionCreateRequest documentDefinitionCreateRequest = new DocumentDefinitionCreateRequest("{\n" +
            "  \"$id\": \"person.schema\",\n" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"title\": \"Person\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"firstName\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"The person's first name.\"\n" +
            "    },\n" +
            "    \"lastName\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"The person's last name.\"\n" +
            "    },\n" +
            "    \"age\": {\n" +
            "      \"description\": \"Age in years which must be equal to or greater than zero.\",\n" +
            "      \"type\": \"integer\",\n" +
            "      \"minimum\": 0\n" +
            "    }\n" +
            "  }\n" +
            "}\n");

        when(documentDefinitionService.deploy(anyString()))
            .thenReturn(new DeployDocumentDefinitionResultSucceeded(definition));

        mockMvc.perform(post("/api/document-definition")
            .content(objectMapper.writeValueAsString(documentDefinitionCreateRequest))
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk());

        verify(documentDefinitionService, times(1)).deploy(anyString());
    }

    @Test
    public void shouldReturnCreateFailedResult() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        DocumentDefinitionCreateRequest documentDefinitionCreateRequest = new DocumentDefinitionCreateRequest("{\n" +
            "  \"$id\": \"person.schema\",\n" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"title\": \"Person\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"firstName\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"The person's first name.\"\n" +
            "    },\n" +
            "    \"lastName\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"The person's last name.\"\n" +
            "    },\n" +
            "    \"age\": {\n" +
            "      \"description\": \"Age in years which must be equal to or greater than zero.\",\n" +
            "      \"type\": \"integer\",\n" +
            "      \"minimum\": 0\n" +
            "    }\n" +
            "  }\n" +
            "}\n");

        when(documentDefinitionService.deploy(anyString()))
            .thenReturn(new DeployDocumentDefinitionResultFailed(List.of(() -> "This schema was already deployed")));

        mockMvc.perform(post("/api/document-definition")
            .content(objectMapper.writeValueAsString(documentDefinitionCreateRequest))
            .characterEncoding(StandardCharsets.UTF_8.name())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(documentDefinitionService, times(1)).deploy(anyString());
    }

    @Test
    public void shouldReturnUndeployDocumentDefinitionSucceeded() throws Exception {
        String definitionName = "documentDefinitionName";

        when(undeployDocumentDefinitionService.undeploy(eq(definitionName))).thenReturn(
            new UndeployDocumentDefinitionResultSucceeded(definitionName)
        );

        mockMvc.perform(delete("/api/document-definition/{name}", definitionName)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk());

        verify(undeployDocumentDefinitionService, times(1)).undeploy(eq(definitionName));
    }

    @Test
    public void shouldReturnUndeployDocumentDefinitionFailed() throws Exception {
        String definitionName = "documentDefinitionName";

        when(undeployDocumentDefinitionService.undeploy(eq(definitionName))).thenReturn(
            new UndeployDocumentDefinitionResultFailed(List.of())
        );

        mockMvc.perform(delete("/api/document-definition/{name}", definitionName)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(undeployDocumentDefinitionService, times(1)).undeploy(eq(definitionName));
    }

}