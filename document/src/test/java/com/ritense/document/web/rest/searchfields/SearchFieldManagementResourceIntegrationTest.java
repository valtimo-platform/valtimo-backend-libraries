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

package com.ritense.document.web.rest.searchfields;

import com.ritense.authorization.AuthorizationService;
import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.service.SearchFieldService;
import com.ritense.document.web.rest.error.DocumentModuleExceptionTranslator;
import com.ritense.document.web.rest.impl.SearchFieldManagementResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.document.domain.impl.searchfield.SearchFieldDataType.TEXT;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldType.SINGLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchType.EXACT;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class SearchFieldManagementResourceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private DocumentModuleExceptionTranslator documentModuleExceptionTranslator;

    @Autowired
    private AuthorizationService authorizationService;

    private MockMvc mockMvc;
    private SearchFieldManagementResource searchFieldManagementResource;
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
        searchFieldManagementResource = new SearchFieldManagementResource(searchFieldService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(searchFieldManagementResource)
                .setControllerAdvice(documentModuleExceptionTranslator)
                .setMessageConverters(mappingJackson2HttpMessageConverter)
                .build();
    }

    @Test
    void shouldRetrieveSearchFieldsByDocumentDefinitionNameForAdmin() throws Exception {
        runWithoutAuthorization(() -> {
            searchFieldService.addSearchField(DOCUMENT_DEFINITION_NAME, SEARCH_FIELD);
            return null;
        });

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

}
