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

import com.ritense.document.BaseTest;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService;
import com.ritense.document.web.rest.impl.JsonSchemaDocumentDefinitionResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JsonSchemaDocumentDefinitionResourceTest extends BaseTest {

    private JsonSchemaDocumentDefinitionService documentDefinitionService;
    private DocumentDefinitionResource documentDefinitionResource;
    private MockMvc mockMvc;
    private Page<JsonSchemaDocumentDefinition> definitionPage;
    private JsonSchemaDocumentDefinition definition;

    @BeforeEach
    public void setUp() {
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        documentDefinitionResource = new JsonSchemaDocumentDefinitionResource(documentDefinitionService);

        mockMvc = MockMvcBuilders.standaloneSetup(documentDefinitionResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

        definition = definition();
        List<JsonSchemaDocumentDefinition> definitions = List.of(definition);
        Pageable unpaged = Pageable.unpaged();

        definitionPage = new PageImpl<>(definitions, unpaged, 1);
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
        when(documentDefinitionService.findLatestByName(anyString())).thenReturn(Optional.of(definition));

        mockMvc.perform(get("/api/document-definition/{name}", definition.getId().name()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

}