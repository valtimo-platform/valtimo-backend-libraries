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

package com.ritense.document.web.rest.searchfields;

import static com.ritense.document.domain.impl.searchfield.SearchFieldDataType.NUMBER;
import static com.ritense.document.domain.impl.searchfield.SearchFieldDataType.TEXT;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldType.SINGLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchType.EXACT;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchType.LIKE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationService;
import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldDataType;
import com.ritense.document.domain.impl.searchfield.SearchFieldDto;
import com.ritense.document.domain.impl.searchfield.SearchFieldFieldType;
import com.ritense.document.domain.impl.searchfield.SearchFieldId;
import com.ritense.document.service.SearchFieldService;
import com.ritense.document.web.rest.error.DocumentModuleExceptionTranslator;
import com.ritense.document.web.rest.impl.SearchFieldMapper;
import com.ritense.document.web.rest.impl.SearchFieldResource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.ritense.valtimo.web.rest.error.WebModuleExceptionTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class SearchFieldResourceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private DocumentModuleExceptionTranslator documentModuleExceptionTranslator;

    @Autowired
    private WebModuleExceptionTranslator webModuleExceptionTranslator;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private SearchFieldResource searchFieldResource;
    private static final String DOCUMENT_DEFINITION_NAME = "house";
    private static final SearchField SEARCH_FIELD = new SearchField(
            "street",
            "doc:street",
            TEXT,
            SINGLE,
            EXACT,
            null,
            0,
            "aTitle"
    );

    @BeforeEach()
    void setUp() {
        searchFieldService = new SearchFieldService(searchFieldRepository, documentDefinitionService,
                authorizationService
        );
        searchFieldResource = new SearchFieldResource(searchFieldService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(searchFieldResource)
                .setControllerAdvice(documentModuleExceptionTranslator, webModuleExceptionTranslator)
                .setMessageConverters(mappingJackson2HttpMessageConverter)
                .build();
    }

    @Test
    void shouldStoreSearchField() throws Exception {
        searchFieldRepository.deleteAllInBatch();

        var searchFieldDto = SearchFieldMapper.toDto(SEARCH_FIELD);

        mockMvc.perform(
                        post("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(objectMapper.writeValueAsString(searchFieldDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());

        // The search field should have been stored in the database
        var storedSearchFields = searchFieldRepository.findAllByIdDocumentDefinitionNameOrderByOrder(DOCUMENT_DEFINITION_NAME);

        assertNotNull(storedSearchFields);
        assertEquals(1, storedSearchFields.size());

        var storedSearchField = storedSearchFields.get(0);

        assertEquals("street", storedSearchField.getKey());
        assertEquals("doc:street", storedSearchField.getPath());
        assertEquals(TEXT, storedSearchField.getDataType());
        assertEquals(SINGLE, storedSearchField.getFieldType());
        assertEquals(EXACT, storedSearchField.getMatchType());
    }

    @Test
    void shouldRespondWith400WhenDataTypeNotTextAndMatchTypeNotExact() throws Exception {
        var searchFieldDto = SearchFieldMapper.toDto(SEARCH_FIELD);
        searchFieldDto.setDataType(SearchFieldDataType.DATE);
        searchFieldDto.setMatchType(LIKE);
        mockMvc.perform(
                        post("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(objectMapper.writeValueAsString(searchFieldDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldRespondWith400WhenDataTypeBooleanAndFieldTypeRange() throws Exception {
        var searchFieldDto = SearchFieldMapper.toDto(SEARCH_FIELD);
        searchFieldDto.setDataType(SearchFieldDataType.BOOLEAN);
        searchFieldDto.setFieldType(SearchFieldFieldType.RANGE);
        mockMvc.perform(
                        post("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(objectMapper.writeValueAsString(searchFieldDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Field type range is invalid for data type boolean")));
    }

    @Test
    void shouldRespondWith400WhenDataTypeDateAndMatchTypeLike() throws Exception {
        var searchFieldDto = SearchFieldMapper.toDto(SEARCH_FIELD);
        searchFieldDto.setDataType(SearchFieldDataType.DATE);
        searchFieldDto.setMatchType(LIKE);
        mockMvc.perform(
                post("/api/management/v1/document-search/{documentDefinitionName}/fields",
                    DOCUMENT_DEFINITION_NAME)
                    .content(objectMapper.writeValueAsString(searchFieldDto))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title", is("Match type like is invalid for data type date")));
    }


    @Test
    void shouldRespondWith400BadRequestOnSearchFieldPathValidationError() throws Exception {
        var searchFieldDto = new SearchFieldDto("lastName", "doc:invalid.path", TEXT, SINGLE, LIKE, null, null);

        mockMvc.perform(
                        post("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(objectMapper.writeValueAsString(searchFieldDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("JsonPath '$.invalid.path' doesn't point to any property inside document definition '" + DOCUMENT_DEFINITION_NAME + "'")));
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = FULL_ACCESS_ROLE)
    void shouldRetrieveSearchFieldsByDocumentDefinitionName() throws Exception {
        searchFieldRepository.deleteAllInBatch();

        var searchFieldDto = SearchFieldMapper.toDto(SEARCH_FIELD);
        mockMvc.perform(
                        post("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(objectMapper.writeValueAsString(searchFieldDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(
                        get("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].key", is("street")))
                .andExpect(jsonPath("$[0].path", is("doc:street")))
                .andExpect(jsonPath("$[0].dataType", is(TEXT.toString())))
                .andExpect(jsonPath("$[0].fieldType", is(SINGLE.toString())))
                .andExpect(jsonPath("$[0].matchType", is(EXACT.toString())))
                .andExpect(jsonPath("$[0].title", is("aTitle")));
    }

    @Test
    void shouldNotRetrieveSearchFieldsWithoutPermissions() throws Exception {
        var searchFieldDto = SearchFieldMapper.toDto(SEARCH_FIELD);
        mockMvc.perform(
                post("/api/management/v1/document-search/{documentDefinitionName}/fields",
                    DOCUMENT_DEFINITION_NAME)
                    .content(objectMapper.writeValueAsString(searchFieldDto))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());
        mockMvc.perform(
                get("/api/management/v1/document-search/{documentDefinitionName}/fields",
                    DOCUMENT_DEFINITION_NAME))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldUpdateSearchField() throws Exception {
        searchFieldRepository.deleteAllInBatch();

        AuthorizationContext.runWithoutAuthorization(() -> {
                searchFieldService.addSearchField(DOCUMENT_DEFINITION_NAME, SEARCH_FIELD);
                return null;
            }
        );
        SearchFieldId searchFieldId = searchFieldRepository.findAllByIdDocumentDefinitionNameOrderByOrder(DOCUMENT_DEFINITION_NAME).get(0).getId();
        SearchFieldDto searchFieldToUpdate = new SearchFieldDto(
                SEARCH_FIELD.getKey(),
                SEARCH_FIELD.getPath(),
                SEARCH_FIELD.getDataType(),
                SearchFieldFieldType.RANGE, //This is the change
                SEARCH_FIELD.getMatchType(),
                SEARCH_FIELD.getDropdownDataProvider(),
                "someTitle");

        mockMvc.perform(
                        put("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(objectMapper.writeValueAsString(List.of(searchFieldToUpdate)))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());

        assertNotNull(searchFieldId);
        Optional<SearchField> searchFieldUpdated = searchFieldRepository.findByIdDocumentDefinitionNameAndKey(
                DOCUMENT_DEFINITION_NAME, searchFieldToUpdate.getKey());
        assertEquals(Boolean.TRUE, searchFieldUpdated.isPresent());
        assertEquals(searchFieldId.getId(), Objects.requireNonNull(searchFieldUpdated.orElseGet(SearchField::new).getId()).getId());
        assertEquals(searchFieldToUpdate.getKey(), searchFieldUpdated.orElseGet(SearchField::new).getKey());
        assertEquals(searchFieldToUpdate.getPath(), searchFieldUpdated.orElseGet(SearchField::new).getPath());
        assertEquals(searchFieldToUpdate.getDataType(), searchFieldUpdated.orElseGet(SearchField::new).getDataType());
        assertEquals(SearchFieldFieldType.RANGE, searchFieldToUpdate.getFieldType());
        assertEquals(searchFieldToUpdate.getMatchType(), searchFieldUpdated.orElseGet(SearchField::new).getMatchType());
        assertEquals(searchFieldToUpdate.getTitle(), searchFieldUpdated.orElseGet(SearchField::new).getTitle());
    }

    @Test
    void shouldReturnBadRequestOnUpdateSearchField() throws Exception {
        SearchFieldDto searchFieldDto = SearchFieldMapper.toDto(SEARCH_FIELD);
        searchFieldDto.setKey("   ");
        mockMvc.perform(
                        put("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(objectMapper.writeValueAsString(List.of(searchFieldDto)))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnChangeOrderingWhenUpdateSearchFields() throws Exception {
        var searchFields = List.of(
                new SearchFieldDto("age", "doc:age", NUMBER, SINGLE, EXACT, null, null),
                new SearchFieldDto("firstName", "doc:firstName", TEXT, SINGLE, LIKE, null, null)
        );
        mockMvc.perform(
                        put("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                "person")
                                .content(objectMapper.writeValueAsString(searchFields))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());

        var searchFieldsResult = searchFieldRepository.findAllByIdDocumentDefinitionNameOrderByOrder("person");
        assertEquals("age", searchFieldsResult.get(0).getKey());
        assertEquals(0, searchFieldsResult.get(0).getOrder());
        assertEquals("firstName", searchFieldsResult.get(1).getKey());
        assertEquals(1, searchFieldsResult.get(1).getOrder());
    }

    @Test
    void shouldDeleteSearchField() throws Exception {
        SearchFieldDto searchFieldDto = SearchFieldMapper.toDto(SEARCH_FIELD);
        mockMvc.perform(
                        post("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME)
                                .content(objectMapper.writeValueAsString(searchFieldDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());
        Optional<SearchField> searchField = searchFieldRepository.findByIdDocumentDefinitionNameAndKey(
                DOCUMENT_DEFINITION_NAME, SEARCH_FIELD.getKey());
        assertTrue(searchField.isPresent());
        mockMvc.perform(
                        delete("/api/management/v1/document-search/{documentDefinitionName}/fields",
                                DOCUMENT_DEFINITION_NAME).queryParam("key", SEARCH_FIELD.getKey())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().isNoContent());
        searchField = searchFieldRepository.findByIdDocumentDefinitionNameAndKey(
                DOCUMENT_DEFINITION_NAME, SEARCH_FIELD.getKey());
        assertTrue(searchField.isEmpty());
    }
}
