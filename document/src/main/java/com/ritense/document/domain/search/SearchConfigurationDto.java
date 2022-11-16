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

package com.ritense.document.domain.search;

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldDataType;
import com.ritense.document.domain.impl.searchfield.SearchFieldFieldType;
import com.ritense.document.domain.impl.searchfield.SearchFieldId;
import com.ritense.document.domain.impl.searchfield.SearchFieldMatchType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchConfigurationDto {

    private List<SearchConfigurationFieldJson> searchFields;

    public SearchConfigurationDto() {
        // Empty constructor needed for Jackson
    }

    public static class SearchConfigurationFieldJson {
        private String key;
        private String path;
        private SearchFieldDataType dataType;
        private SearchFieldFieldType fieldType;
        private SearchFieldMatchType matchType;
        private String title;

        public SearchConfigurationFieldJson() {
            // Empty constructor needed for Jackson
        }

        public SearchField toEntity(String documentDefinitionName, int order) {
            var searchField = new SearchField(key, path, dataType, fieldType, matchType, order, title);
            searchField.setId(SearchFieldId.newId(documentDefinitionName));
            return searchField;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public SearchFieldDataType getDataType() {
            return dataType;
        }

        public void setDataType(SearchFieldDataType dataType) {
            this.dataType = dataType;
        }

        public SearchFieldFieldType getFieldType() {
            return fieldType;
        }

        public void setFieldType(SearchFieldFieldType fieldType) {
            this.fieldType = fieldType;
        }

        public SearchFieldMatchType getMatchType() {
            return matchType;
        }

        public void setMatchType(SearchFieldMatchType matchType) {
            this.matchType = matchType;
        }


        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public List<SearchField> toEntity(String documentDefinitionName) {
        return IntStream.range(0, searchFields.size())
            .mapToObj(index -> searchFields.get(index).toEntity(documentDefinitionName, index))
            .collect(Collectors.toList());
    }

    public List<SearchConfigurationFieldJson> getSearchFields() {
        return searchFields;
    }

    public void setSearchFields(List<SearchConfigurationFieldJson> searchFields) {
        this.searchFields = searchFields;
    }
}
