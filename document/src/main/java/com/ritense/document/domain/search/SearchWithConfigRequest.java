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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;

public class SearchWithConfigRequest {
    private String createdBy;
    private Long sequence;
    private SearchOperator searchOperator = SearchOperator.OR;
    private List<SearchWithConfigFilter> otherFilters = List.of();

    public SearchWithConfigRequest() {
    }

    public SearchWithConfigRequest(
        String createdBy,
        Long sequence,
        SearchOperator searchOperator,
        List<SearchWithConfigFilter> otherFilters
    ) {
        this.createdBy = createdBy;
        this.sequence = sequence;
        this.searchOperator = searchOperator;
        this.otherFilters = otherFilters;
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

    public List<SearchWithConfigFilter> getOtherFilters() {
        return otherFilters;
    }

    public void setOtherFilters(List<SearchWithConfigFilter> otherFilters) {
        this.otherFilters = otherFilters;
    }

    public static class SearchWithConfigFilter {

        @NotNull
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
