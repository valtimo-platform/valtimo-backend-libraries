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

package com.ritense.document.domain.search;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;

public class SearchWithConfigRequest {
    private SearchOperator searchOperator = SearchOperator.AND;
    private AssigneeFilter assigneeFilter = AssigneeFilter.ALL;
    private List<SearchWithConfigFilter> otherFilters = List.of();
    private String tenantId;

    public SearchWithConfigRequest() {
    }

    public SearchWithConfigRequest(
        SearchOperator searchOperator,
        List<SearchWithConfigFilter> otherFilters
    ) {
        this.searchOperator = searchOperator;
        this.otherFilters = otherFilters;
    }

    public SearchOperator getSearchOperator() {
        return searchOperator;
    }

    public void setSearchOperator(SearchOperator searchOperator) {
        this.searchOperator = searchOperator;
    }

    public AssigneeFilter getAssigneeFilter() {
        return assigneeFilter;
    }

    public void setAssigneeFilter(AssigneeFilter assigneeFilter) {
        this.assigneeFilter = assigneeFilter;
    }

    public List<SearchWithConfigFilter> getOtherFilters() {
        return otherFilters;
    }

    public void setOtherFilters(List<SearchWithConfigFilter> otherFilters) {
        this.otherFilters = otherFilters;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public static class SearchWithConfigFilter {

        private String key;
        private SearchRequestValue rangeFrom = SearchRequestValue.ofNull();
        private SearchRequestValue rangeTo = SearchRequestValue.ofNull();
        private List<SearchRequestValue> values = List.of();

        public SearchWithConfigFilter() {
        }

        public <T extends Comparable<? super T>> SearchWithConfigFilter(String key, T rangeFrom, T rangeTo, List<Object> values) {
            assertArgumentNotEmpty(key, "key is required");
            this.key = key;
            this.rangeFrom = SearchRequestValue.ofComparable(rangeFrom);
            this.rangeTo = SearchRequestValue.ofComparable(rangeTo);
            this.values = SearchRequestValue.ofList(values);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public <T extends Comparable<? super T>> T getRangeFrom() {
            return rangeFrom.getComparableValue();
        }

        @JsonIgnore
        public SearchRequestValue getRangeFromSearchRequestValue() {
            return rangeFrom;
        }

        public void setRangeFrom(Object rangeFrom) {
            this.rangeFrom = SearchRequestValue.ofComparable(rangeFrom);
        }

        public <T extends Comparable<? super T>> T getRangeTo() {
            return rangeTo.getComparableValue();
        }

        @JsonIgnore
        public SearchRequestValue getRangeToSearchRequestValue() {
            return rangeTo;
        }

        public void setRangeTo(Object rangeTo) {
            this.rangeTo = SearchRequestValue.ofComparable(rangeTo);
        }

        public <T> List<T> getValues() {
            return values.stream()
                .map(SearchRequestValue::<T>getValue)
                .collect(Collectors.toList());
        }

        @JsonIgnore
        public List<SearchRequestValue> getSearchRequestValues() {
            return values;
        }

        public void setValues(List<Object> values) {
            this.values = SearchRequestValue.ofList(values);
        }
    }
}
