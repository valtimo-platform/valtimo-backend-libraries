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

package com.ritense.document.web.rest;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.document.BaseTest;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.assignee.UnassignedDocumentCountDto;
import com.ritense.document.domain.impl.template.DocumentDefinitionTemplateRequestDto;
import com.ritense.document.service.DocumentStatisticService;
import com.ritense.document.service.UndeployDocumentDefinitionService;
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService;
import com.ritense.document.service.impl.UndeployJsonSchemaDocumentDefinitionService;
import com.ritense.document.service.request.DocumentDefinitionCreateRequest;
import com.ritense.document.service.result.DeployDocumentDefinitionResultFailed;
import com.ritense.document.service.result.DeployDocumentDefinitionResultSucceeded;
import com.ritense.document.service.result.UndeployDocumentDefinitionResultFailed;
import com.ritense.document.service.result.UndeployDocumentDefinitionResultSucceeded;
import com.ritense.document.web.rest.impl.JsonSchemaDocumentDefinitionResource;
import com.ritense.valtimo.contract.json.MapperSingleton;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class JsonSchemaDocumentDefinitionResourceTest extends BaseTest {

    public static final String SOME_ROLE = "SOME_ROLE";
    private JsonSchemaDocumentDefinitionService documentDefinitionService;
    private DocumentDefinitionResource documentDefinitionResource;
    private UndeployDocumentDefinitionService undeployDocumentDefinitionService;
    private DocumentStatisticService documentStatisticService;
    private MockMvc mockMvc;
    private Page<JsonSchemaDocumentDefinition> definitionPage;
    private JsonSchemaDocumentDefinition definition;

    @BeforeEach
    public void setUp() {
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        undeployDocumentDefinitionService = mock(UndeployJsonSchemaDocumentDefinitionService.class);
        documentStatisticService = mock(DocumentStatisticService.class);

        documentDefinitionResource = new JsonSchemaDocumentDefinitionResource(
            documentDefinitionService,
            undeployDocumentDefinitionService,
            documentStatisticService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(documentDefinitionResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(MapperSingleton.get()))
            .build();

        definition = definition();
        List<JsonSchemaDocumentDefinition> definitions = List.of(definition);
        Pageable unpaged = Pageable.unpaged();

        definitionPage = new PageImpl<>(definitions, unpaged, 1);
    }

    @Test
    void shouldReturnPagedRecordPage() throws Exception {
        when(documentDefinitionService.findAll(any())).thenReturn(definitionPage);

        mockMvc.perform(get("/api/v1/document-definition"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void shouldReturnTemplate() throws Exception {
        var objectMapper = MapperSingleton.get();
        var requestDto = new DocumentDefinitionTemplateRequestDto("123", "456");

        mockMvc.perform(
            post("/api/management/v1/document-definition-template")
                .content(objectMapper.writeValueAsString(requestDto))
                .characterEncoding(StandardCharsets.UTF_8.name())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().json(
                """
                {
                    "$id": "123.schema",
                    "type": "object",
                    "title": "456",
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "properties": {},
                    "additionalProperties":false
                }
                """
            ));
    }

    @Test
    void shouldNotReturnTemplateWhenIdEndsOnPeriod() throws Exception {
        var objectMapper = MapperSingleton.get();
        var requestDto = new DocumentDefinitionTemplateRequestDto("123.", "456");

        mockMvc.perform(
                post("/api/management/v1/document-definition-template")
                    .content(objectMapper.writeValueAsString(requestDto))
                    .characterEncoding(StandardCharsets.UTF_8.name())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnPagedRecordPageWithOldSortByNameProperty() throws Exception {
        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(documentDefinitionService.findAll(pageCaptor.capture())).thenReturn(definitionPage);

        mockMvc.perform(get("/api/v1/document-definition?sort=id.name,DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());

        Pageable page = pageCaptor.getValue();
        assertEquals("document_definition_name", page.getSort().getOrderFor("document_definition_name").getProperty());
        assertEquals(Sort.Direction.DESC, page.getSort().getOrderFor("document_definition_name").getDirection());
    }

    @Test
    void shouldReturnPagedRecordPageWithOldSortByVersionProperty() throws Exception {
        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(documentDefinitionService.findAll(pageCaptor.capture())).thenReturn(definitionPage);

        mockMvc.perform(get("/api/v1/document-definition?sort=id.version,DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());

        Pageable page = pageCaptor.getValue();
        assertEquals("document_definition_version", page.getSort().getOrderFor("document_definition_version").getProperty());
        assertEquals(Sort.Direction.DESC, page.getSort().getOrderFor("document_definition_version").getDirection());
    }

    @Test
    void shouldReturnPagedRecordPageWithMultipleOrderProperties() throws Exception {
        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(documentDefinitionService.findAll(pageCaptor.capture())).thenReturn(definitionPage);

        mockMvc.perform(get("/api/v1/document-definition?sort=readOnly,ASC&sort=id.name,DESC&sort=other,ASC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());

        Pageable page = pageCaptor.getValue();
        assertEquals("document_definition_name", page.getSort().getOrderFor("document_definition_name").getProperty());
        assertEquals(Sort.Direction.DESC, page.getSort().getOrderFor("document_definition_name").getDirection());
        assertEquals("read_only", page.getSort().getOrderFor("read_only").getProperty());
        assertEquals(Sort.Direction.ASC, page.getSort().getOrderFor("read_only").getDirection());
        //also include 1 property that is not in the mapping
        assertEquals("other", page.getSort().getOrderFor("other").getProperty());
        assertEquals(Sort.Direction.ASC, page.getSort().getOrderFor("other").getDirection());
    }

    @Test
    void shouldReturnSingleDefinitionRecordByName() throws Exception {
        String definitionName = definition.getId().name();
        when(documentDefinitionService.findLatestByName(anyString())).thenReturn(Optional.of(definition));
        mockMvc.perform(get("/api/v1/document-definition/{name}", definitionName))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void shouldReturnSingleDefinitionRecordByNameForManagement() throws Exception {
        String definitionName = definition.getId().name();
        when(documentDefinitionService.findLatestByName(anyString())).thenReturn(Optional.of(definition));
        mockMvc.perform(get("/api/management/v1/document-definition/{name}", definitionName))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());

        verify(documentDefinitionService).findLatestByName(definitionName);
    }

    @Test
    void shouldReturnSingleDefinitionRecordByNameAndVersion() throws Exception {
        String definitionName = definition.getId().name();
        long definitionVersion = definition.id().version();
        when(documentDefinitionService.findByNameAndVersion(definitionName, definitionVersion)).thenReturn(Optional.of(definition));
        mockMvc.perform(get("/api/management/v1/document-definition/{name}/version/{version}", definitionName, definitionVersion))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void shouldReturnNoDefinitionRecordByNameAndVersion() throws Exception {
        String definitionName = definition.getId().name();
        long definitionVersion = 5;
        when(documentDefinitionService.findByNameAndVersion(definitionName, definition.getId().version())).thenReturn(Optional.of(definition));
        mockMvc.perform(get("/api/management/v1/document-definition/{name}/version/{version}", definitionName, definitionVersion))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnCreateSuccessResult() throws Exception {
        ObjectMapper objectMapper = MapperSingleton.INSTANCE.get();
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

        mockMvc.perform(post("/api/v1/document-definition")
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
    void shouldReturnCreateFailedResult() throws Exception {
        ObjectMapper objectMapper = MapperSingleton.INSTANCE.get();
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

        mockMvc.perform(post("/api/v1/document-definition")
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
    void shouldReturnUndeployDocumentDefinitionSucceeded() throws Exception {
        String definitionName = "documentDefinitionName";

        when(undeployDocumentDefinitionService.undeploy(eq(definitionName))).thenReturn(
            new UndeployDocumentDefinitionResultSucceeded(definitionName)
        );

        mockMvc.perform(delete("/api/v1/document-definition/{name}", definitionName)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk());

        verify(undeployDocumentDefinitionService, times(1)).undeploy(eq(definitionName));
    }

    @Test
    void shouldReturnUndeployDocumentDefinitionFailed() throws Exception {
        String definitionName = "documentDefinitionName";

        when(undeployDocumentDefinitionService.undeploy(eq(definitionName))).thenReturn(
            new UndeployDocumentDefinitionResultFailed(List.of())
        );

        mockMvc.perform(delete("/api/v1/document-definition/{name}", definitionName)
            .accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(undeployDocumentDefinitionService, times(1)).undeploy(eq(definitionName));
    }

    @Test
    void shouldReturnUnassignedDocumentCount() throws Exception {
        when(documentStatisticService.getUnassignedDocumentCountDtos()).thenReturn(
            List.of(new UnassignedDocumentCountDto("my-document-definition-name", 23L))
        );

        mockMvc.perform(
                get("/api/v1/document-definition/open/count").accept(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[0].documentDefinitionName").value("my-document-definition-name"))
            .andExpect(jsonPath("$.[0].openDocumentCount").value(23L));
    }

}
