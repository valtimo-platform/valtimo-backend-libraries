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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchRequest2 {
    private SearchOperator searchOperator = SearchOperator.OR;
    private List<SearchCriteria2> otherFilters = List.of();

    public SearchRequest2() {
        // Jackson needs the empty constructor
    }

    public SearchOperator getSearchOperator() {
        return searchOperator;
    }

    public void setSearchOperator(SearchOperator searchOperator) {
        this.searchOperator = searchOperator;
    }

    public SearchRequest2 searchOperator(SearchOperator searchOperator) {
        setSearchOperator(searchOperator);
        return this;
    }

    public List<SearchCriteria2> getOtherFilters() {
        return otherFilters;
    }

    public void setOtherFilters(List<SearchCriteria2> otherFilters) {
        this.otherFilters = otherFilters;
    }

    public SearchRequest2 addOtherFilters(SearchCriteria2 otherFilter) {
        this.otherFilters = new ArrayList<>(otherFilters);
        otherFilters.add(otherFilter);
        return this;
    }

    public static class SearchCriteria2 {

        private String path;
        private DatabaseSearchType searchType = DatabaseSearchType.EQUAL;
        private SearchRequestValue rangeFrom = SearchRequestValue.ofNull();
        private SearchRequestValue rangeTo = SearchRequestValue.ofNull();
        private List<SearchRequestValue> values = List.of();

        public SearchCriteria2() {
            // Jackson needs the empty constructor
        }

        public <T> Class<T> getDataType() {
            if (rangeFrom.isNotNull()) {
                return rangeFrom.getValueClass();
            } else if (rangeTo.isNotNull()) {
                return rangeTo.getValueClass();
            } else if (!values.isEmpty()) {
                return values.get(0).getValueClass();
            } else {
                throw new IllegalStateException("SearchCriteria has no values");
            }
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public SearchCriteria2 path(String path) {
            setPath(path);
            return this;
        }

        public DatabaseSearchType getSearchType() {
            return searchType;
        }

        public void setSearchType(DatabaseSearchType searchType) {
            this.searchType = searchType;
        }

        public SearchCriteria2 searchType(DatabaseSearchType searchType) {
            setSearchType(searchType);
            return this;
        }

        public <T extends Comparable<? super T>> T getRangeFrom() {
            return rangeFrom.getComparableValue();
        }

        public void setRangeFrom(Object rangeFrom) {
            this.rangeFrom = SearchRequestValue.ofComparable(rangeFrom);
        }

        public SearchCriteria2 rangeFrom(Object rangeFrom) {
            setRangeFrom(rangeFrom);
            return this;
        }

        public <T extends Comparable<? super T>> T getRangeTo() {
            return rangeTo.getComparableValue();
        }

        public void setRangeTo(Object rangeTo) {
            this.rangeTo = SearchRequestValue.ofComparable(rangeTo);
        }

        public <T extends Comparable<? super T>> SearchCriteria2 rangeTo(T rangeTo) {
            setRangeTo(rangeTo);
            return this;
        }

        public <T> List<T> getValues() {
            return values.stream()
                .map(SearchRequestValue::<T>getValue)
                .collect(Collectors.toList());
        }

        public void setValues(List<Object> values) {
            this.values = SearchRequestValue.ofList(values);
        }

        public void setSearchRequestValues(List<SearchRequestValue> searchRequestValues) {
            this.values = searchRequestValues;
        }

        public SearchCriteria2 addValue(Object value) {
            this.values = new ArrayList<>(values);
            values.add(SearchRequestValue.of(value));
            return this;
        }
    }
}
