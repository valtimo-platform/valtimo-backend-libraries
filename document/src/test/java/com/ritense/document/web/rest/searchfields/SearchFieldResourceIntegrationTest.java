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

package com.ritense.document.web.rest.searchfields;

import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.impl.Mapper;
import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldDatatype;
import com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype;
import com.ritense.document.domain.impl.searchfield.SearchFieldId;
import com.ritense.document.domain.impl.searchfield.SearchFieldMatchtype;
import com.ritense.document.service.SearchFieldService;
import com.ritense.document.web.rest.impl.SearchFieldResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchFieldResourceIntegrationTest extends BaseIntegrationTest {

    private MockMvc mockMvc;

    private SearchFieldResource searchFieldResource;
    private static final String DOCUMENT_DEFINITION_NAME = "test_document";
    private static final SearchField SEARCH_FIELD = new SearchField(
            "someKey",
            "/some/path",
            SearchFieldDatatype.TEXT,
            SearchFieldFieldtype.SINGLE,
            SearchFieldMatchtype.EXACT
    );

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
        mockMvc.perform(
                post("/api/v1/document-search/{documentDefinitionName}/fields",
                     DOCUMENT_DEFINITION_NAME)
                    .content(Mapper.INSTANCE.get().writeValueAsString(SEARCH_FIELD))
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

    @Test
    void shouldRetrieveSearchFieldsByDocumentDefinitionName() throws Exception {
        mockMvc.perform(
                get("/api/v1/document-search/{documentDefinitionName}/fields",
                    "test_document"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].key",is("someKey")))
            .andExpect(jsonPath("$[0].path",is("/some/path")))
            .andExpect(jsonPath("$[0].datatype",is(SearchFieldDatatype.TEXT.toString())))
            .andExpect(jsonPath("$[0].fieldtype",is(SearchFieldFieldtype.SINGLE.toString())))
            .andExpect(jsonPath("$[0].matchtype",is(SearchFieldMatchtype.EXACT.toString())));
    }

    @Test
    void shouldUpdateSearchField() throws Exception{
        searchFieldService.addSearchField(DOCUMENT_DEFINITION_NAME,SEARCH_FIELD);
        SearchFieldId searchFieldId = searchFieldService.getSearchFields(DOCUMENT_DEFINITION_NAME).get(0).getId();
        SearchField searchFieldToUpdate = new SearchField(
                SEARCH_FIELD.getKey(),
                SEARCH_FIELD.getPath(),
                SEARCH_FIELD.getDatatype(),
                SearchFieldFieldtype.RANGE, //This is the change
                SEARCH_FIELD.getMatchtype());
        searchFieldToUpdate.setId(searchFieldId);
        mockMvc.perform(
                        put("/api/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(Mapper.INSTANCE.get().writeValueAsString(searchFieldToUpdate))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());
        assertNotNull(searchFieldToUpdate.getId());

        Optional<SearchField> searchFieldUpdated =searchFieldRepository.findById(searchFieldToUpdate.getId());
        assertEquals(Boolean.TRUE,searchFieldUpdated.isPresent());
        assertEquals(searchFieldToUpdate.getId(),searchFieldUpdated.orElseGet(SearchField::new).getId());
        assertEquals(searchFieldToUpdate.getKey(),searchFieldUpdated.orElseGet(SearchField::new).getKey());
        assertEquals(searchFieldToUpdate.getPath(),searchFieldUpdated.orElseGet(SearchField::new).getPath());
        assertEquals(searchFieldToUpdate.getDatatype(),searchFieldUpdated.orElseGet(SearchField::new).getDatatype());
        assertEquals(searchFieldToUpdate.getFieldtype(),SearchFieldFieldtype.RANGE);
        assertEquals(searchFieldToUpdate.getMatchtype(),searchFieldUpdated.orElseGet(SearchField::new).getMatchtype());
    }

    @Test
    void shouldReturnBadRequestOnUpdateSearchField() throws Exception{
        mockMvc.perform(
                        put("/api/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(Mapper.INSTANCE.get().writeValueAsString(SEARCH_FIELD))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
