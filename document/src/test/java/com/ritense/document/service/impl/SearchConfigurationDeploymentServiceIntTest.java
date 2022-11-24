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

package com.ritense.document.service.impl;

import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.service.SearchFieldService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ritense.document.domain.impl.searchfield.SearchFieldDataType.TEXT;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldType.SINGLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchType.EXACT;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchType.LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
class SearchConfigurationDeploymentServiceIntTest extends BaseIntegrationTest {

    @Autowired
    public SearchFieldService searchFieldService;

    @Test
    void shouldDeploySearchConfigurationFromResourceFolder() {
        var documentDefinitionName = "profile";

        var searchFields = searchFieldService.getSearchFields(documentDefinitionName);

        assertThat(searchFields).hasSize(2);
        assertThat(searchFields.get(0).getId().getDocumentDefinitionName()).isEqualTo("profile");
        assertThat(searchFields.get(0).getKey()).isEqualTo("firstName");
        assertThat(searchFields.get(0).getPath()).isEqualTo("doc:firstName");
        assertThat(searchFields.get(0).getDataType()).isEqualTo(TEXT);
        assertThat(searchFields.get(0).getFieldType()).isEqualTo(SINGLE);
        assertThat(searchFields.get(0).getMatchType()).isEqualTo(LIKE);
        assertThat(searchFields.get(0).getOrder()).isZero();
        assertThat(searchFields.get(1).getId().getDocumentDefinitionName()).isEqualTo("profile");
        assertThat(searchFields.get(1).getKey()).isEqualTo("lastName");
        assertThat(searchFields.get(1).getPath()).isEqualTo("doc:lastName");
        assertThat(searchFields.get(1).getDataType()).isEqualTo(TEXT);
        assertThat(searchFields.get(1).getFieldType()).isEqualTo(SINGLE);
        assertThat(searchFields.get(1).getMatchType()).isEqualTo(LIKE);
        assertThat(searchFields.get(1).getOrder()).isOne();
    }

    @Test
    void shouldFailToDeployDueToDuplicateKeys() {
        List<SearchField> searchFields = new ArrayList<>();
        searchFields.add(new SearchField(
                "someKey",
                "doc:some.path",
                TEXT,
                SINGLE,
                EXACT,
                0,
                null
        ));
        searchFields.add(new SearchField(
                "someKey",
                "doc:some.path",
                TEXT,
                SINGLE,
                LIKE,
                1,
                null
        ));
        searchFieldService.createSearchConfiguration(searchFields);
        searchFields = searchFieldService.getSearchFields("aDefinitionName");
        assertThat(searchFields).isEmpty();
    }

    @Test
    void shouldThrowExceptionDueToDuplicateKey() {
        SearchField searchField = new SearchField("someKey",
                "doc:somePath",
                TEXT,
                SINGLE,
                LIKE,
                0,
                null);
        searchFieldService.addSearchField("aDefinitionName", searchField);
        assertThrows(IllegalArgumentException.class,
                () -> searchFieldService.addSearchField("aDefinitionName", searchField));
    }

}
