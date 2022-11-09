package com.ritense.document.domain.search;

import java.util.List;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;

public class SearchWithConfigRequest {
    private String createdBy;
    private Long sequence;
    private SearchOperator searchOperator = SearchOperator.OR;
    private List<SearchWithConfigFilter> otherFilters;

    public static class SearchWithConfigFilter {

        private String key;
        private Object rangeFrom;
        private Object rangeTo;
        private List<Object> values;

        public SearchWithConfigFilter(String key, Object rangeFrom, Object rangeTo, List<Object> values) {
            assertArgumentNotEmpty(key, "key is required");
            this.key = key;
            this.rangeFrom = rangeFrom;
            this.rangeTo = rangeTo;
            this.values = values;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
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

    public List<SearchWithConfigFilter> getOtherFilters() {
        return otherFilters;
    }

    public void setOtherFilters(List<SearchWithConfigFilter> otherFilters) {
        this.otherFilters = otherFilters;
    }
}
