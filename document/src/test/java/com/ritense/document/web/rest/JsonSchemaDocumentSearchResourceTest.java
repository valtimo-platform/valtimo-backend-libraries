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
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.service.DocumentSearchService;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.document.service.impl.JsonSchemaDocumentSearchService;
import com.ritense.document.service.impl.SearchCriteria;
import com.ritense.document.web.rest.impl.JsonSchemaDocumentSearchResource;
import com.ritense.valtimo.contract.utils.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JsonSchemaDocumentSearchResourceTest extends BaseTest {

    private DocumentSearchService documentSearchService;
    private DocumentSequenceGeneratorService documentSequenceGeneratorService;
    private DocumentSearchResource documentSearchResource;
    private MockMvc mockMvc;
    private Page<? extends Document> documentPage;
    private JsonSchemaDocument document;
    private static final String USERNAME = "test@test.com";

    @BeforeEach
    public void beforeAll() {
        documentSequenceGeneratorService = mock(DocumentSequenceGeneratorService.class);
        when(documentSequenceGeneratorService.next(any())).thenReturn(1L);

        documentSearchService = mock(JsonSchemaDocumentSearchService.class);
        documentSearchResource = new JsonSchemaDocumentSearchResource(documentSearchService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(documentSearchResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

        var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
        final JsonSchemaDocument.CreateDocumentResultImpl result = JsonSchemaDocument.create(
            definition(),
            content,
            USERNAME,
            documentSequenceGeneratorService,
            null
        );
        document = result.resultingDocument().orElseThrow();

        List<? extends Document> documents = List.of(document);
        Pageable unPaged = Pageable.unpaged();

        documentPage = new PageImpl<>(documents, unPaged, 1);
    }

    @Test
    public void shouldReturnOkWithSearchCriteria() throws Exception {
        when(documentSearchService.search(any(), any(), any())).thenReturn(Page.empty());

        List<SearchCriteria> values = Arrays.asList(
            new SearchCriteria("aPath", "aValue"),
            new SearchCriteria("aPath2", "aValue2")
        );

        mockMvc.perform(
            post("/api/document-search/{document-definition-name}", "definition")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(TestUtil.convertObjectToJsonBytes(values))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("*").isArray());
    }

    @Test
    public void shouldReturnOkWhenSearchingDocumentWithDefinition() throws Exception {
        final String definitionName = "house";
        when(documentSearchService.search(any(), any(), any(), any(), any())).thenReturn(new PageImpl(List.of(document), Pageable.unpaged(), 1));

        mockMvc.perform(get("/api/document-search")
            .accept(APPLICATION_JSON_VALUE)
            .contentType(APPLICATION_JSON_VALUE)
            .param("searchCriteria", "someValue")
            .param("definitionName", definitionName)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void shouldReturnPagedRecordPageWithoutSearchParams() throws Exception {
        when(documentSearchService.search(any(), any(), any(), any(), any())).thenReturn(new PageImpl(List.of(document), Pageable.unpaged(), 1));

        mockMvc.perform(get("/api/document-search")
            .accept(APPLICATION_JSON_VALUE)
            .contentType(APPLICATION_JSON_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void shouldReturnOkWhenSearchingDocument() throws Exception {
        when(documentSearchService.search(any(), any(), any(), any(), any())).thenReturn(new PageImpl(List.of(document), Pageable.unpaged(), 1));

        mockMvc.perform(get("/api/document-search")
            .accept(APPLICATION_JSON_VALUE)
            .contentType(APPLICATION_JSON_VALUE)
            .param("searchCriteria", "someValue")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());
    }

}