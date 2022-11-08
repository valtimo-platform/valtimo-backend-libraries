/*
 * Copyright 015-0 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1. (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.document.domain.search;

import java.util.List;

public class SearchRequest2 {
    private final String createdBy;
    private final Long sequence;
    private final SearchOperator searchOperator;
    private final List<SearchCriteria2> otherFilters;

    public SearchRequest2(String createdBy, Long sequence, SearchOperator searchOperator, List<SearchCriteria2> otherFilters) {
        this.createdBy = createdBy;
        this.sequence = sequence;
        this.searchOperator = searchOperator;
        this.otherFilters = otherFilters;
    }

    public static class SearchCriteria2 {

        private final String path;
        private final DatabaseSearchType searchType;
        private final Object rangeFrom;
        private final Object rangeTo;
        private final List<Object> values;

        public SearchCriteria2(String path, DatabaseSearchType searchType, Object rangeFrom, Object rangeTo, List<Object> values) {
            this.path = path;
            this.searchType = searchType;
            this.rangeFrom = rangeFrom;
            this.rangeTo = rangeTo;
            this.values = values;
        }

        public String getPath() {
            return path;
        }

        public DatabaseSearchType getSearchType() {
            return searchType;
        }

        public Object getRangeFrom() {
            return rangeFrom;
        }

        public Object getRangeTo() {
            return rangeTo;
        }

        public List<Object> getValues() {
            return values;
        }

        public Class<?> getDataType() {
            if (rangeFrom != null) {
                return rangeFrom.getClass();
            } else if (rangeTo != null) {
                return rangeTo.getClass();
            } else if (values != null && !values.isEmpty()) {
                return values.get(0).getClass();
            } else {
                return String.class;
            }
        }
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Long getSequence() {
        return sequence;
    }

    public SearchOperator getSearchOperator() {
        return searchOperator;
    }

    public List<SearchCriteria2> getOtherFilters() {
        return otherFilters;
    }
}
