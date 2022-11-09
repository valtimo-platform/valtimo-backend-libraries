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
    private String createdBy;
    private Long sequence;
    private SearchOperator searchOperator;
    private List<SearchCriteria2> otherFilters;

    public SearchRequest2() {
    }

    public static class SearchCriteria2 {

        private String path;
        private DatabaseSearchType searchType;
        private Object rangeFrom;
        private Object rangeTo;
        private List<Object> values;

        public SearchCriteria2() {
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

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public DatabaseSearchType getSearchType() {
            return searchType;
        }

        public void setSearchType(DatabaseSearchType searchType) {
            this.searchType = searchType;
        }

        public Object getRangeFrom() {
            return rangeFrom;
        }

        public void setRangeFrom(Object rangeFrom) {
            this.rangeFrom = rangeFrom;
        }

        public Object getRangeTo() {
            return rangeTo;
        }

        public void setRangeTo(Object rangeTo) {
            this.rangeTo = rangeTo;
        }

        public List<Object> getValues() {
            return values;
        }

        public void setValues(List<Object> values) {
            this.values = values;
        }
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public SearchOperator getSearchOperator() {
        return searchOperator;
    }

    public void setSearchOperator(SearchOperator searchOperator) {
        this.searchOperator = searchOperator;
    }

    public List<SearchCriteria2> getOtherFilters() {
        return otherFilters;
    }

    public void setOtherFilters(List<SearchCriteria2> otherFilters) {
        this.otherFilters = otherFilters;
    }
}
