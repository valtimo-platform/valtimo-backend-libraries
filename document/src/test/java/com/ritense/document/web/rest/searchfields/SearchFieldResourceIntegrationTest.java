/*
 *  Copyright 2015-2022 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.document.web.rest.searchfields;

import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.Mapper;
import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldDatatype;
import com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype;
import com.ritense.document.domain.impl.searchfield.SearchFieldMatchtype;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.service.SearchFieldService;
import com.ritense.document.web.rest.impl.SearchFieldResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchFieldResourceIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;
    private SearchFieldResource searchFieldResource;

    @BeforeEach()
    void setUp() {
        searchFieldService = new SearchFieldService(searchFieldRepository);
        searchFieldResource = new SearchFieldResource(searchFieldService);

        mockMvc = MockMvcBuilders
            .standaloneSetup(searchFieldResource)
            .build();
    }

    @Test
    void shouldStoreSearchField() throws Exception {
        var searchField = new SearchField(
            "someKey",
            "/some/path",
            SearchFieldDatatype.TEXT,
            SearchFieldFieldtype.SINGLE,
            SearchFieldMatchtype.EXACT
        );

        mockMvc.perform(
                post("/api/v1/document-search/{documentDefinitionName}/fields",
                     "test_document")
                    .content(Mapper.INSTANCE.get().writeValueAsString(searchField))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        // The search field should have been stored in the database
        var storedSearchFields = searchFieldRepository.findAllByIdDocumentDefinitionName("test_document");

        assertNotNull(storedSearchFields);
        assertEquals(1, storedSearchFields.size());

        var storedSearchField = storedSearchFields.get(0);

        assertEquals("someKey", storedSearchField.getKey());
        assertEquals("/some/path", storedSearchField.getPath());
        assertEquals(SearchFieldDatatype.TEXT, storedSearchField.getDatatype());
        assertEquals(SearchFieldFieldtype.SINGLE, storedSearchField.getFieldtype());
        assertEquals(SearchFieldMatchtype.EXACT, storedSearchField.getMatchtype());
    }
}
