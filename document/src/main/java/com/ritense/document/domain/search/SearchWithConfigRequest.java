package com.ritense.document.domain.search;

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.exception.SearchConfigRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.MULTIPLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.RANGE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.SINGLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchtype.EXACT;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchtype.LIKE;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;

public class SearchWithConfigRequest {
    private String createdBy;
    private Long sequence;
    private SearchOperator searchOperator = SearchOperator.OR;
    private List<SearchWithConfigFilter> otherFilters;

    public SearchRequest2 toSearchRequest(List<SearchRequest2.SearchCriteria2> otherFilters) {
        return new SearchRequest2(createdBy, sequence, searchOperator, otherFilters);
    }

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

        public SearchRequest2.SearchCriteria2 toSearchCriteria(SearchField searchField) {
            assertSearchFieldType(searchField);
            assertDataType(searchField);
            var searchType = findDatabaseSearchType(searchField);
            return new SearchRequest2.SearchCriteria2(searchField.getPath(), searchType, rangeFrom, rangeTo, values);
        }

        private DatabaseSearchType findDatabaseSearchType(SearchField searchField) {
            if (searchField.getFieldtype() == MULTIPLE) {
                return DatabaseSearchType.IN;
            } else if (searchField.getFieldtype() == RANGE) {
                if (rangeFrom != null && rangeTo != null) {
                    return DatabaseSearchType.BETWEEN;
                } else if (rangeFrom != null) {
                    return DatabaseSearchType.FROM;
                } else if (rangeTo != null) {
                    return DatabaseSearchType.TO;
                } else {
                    throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "range parameters were not found");
                }
            } else if (searchField.getMatchtype() == LIKE) {
                return DatabaseSearchType.LIKE;
            } else if (searchField.getMatchtype() == EXACT) {
                return DatabaseSearchType.EXACT;
            } else {
                throw new IllegalStateException("Unknown match type: " + searchField.getMatchtype());
            }
        }

        private void assertSearchFieldType(SearchField searchField) {
            if (searchField.getFieldtype() == MULTIPLE || searchField.getFieldtype() == SINGLE) {
                if (rangeFrom != null || rangeTo != null) {
                    throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "range parameters were found");
                }
                if (values == null) {
                    throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "no values were found");
                }
            }
            if (searchField.getFieldtype() == SINGLE && values != null && values.isEmpty()) {
                throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "no value was found");
            }
            if (searchField.getFieldtype() == SINGLE && values != null && values.size() >= 2) {
                throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "multiple values were found");
            }
            if (searchField.getFieldtype() == RANGE && values != null && !values.isEmpty()) {
                throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "individual values were found");
            }
        }

        private void assertDataType(SearchField searchField) {
            var allValues = new ArrayList<>(values);
            if (rangeFrom != null) {
                allValues.add(rangeFrom);
            }
            if (rangeTo != null) {
                allValues.add(rangeTo);
            }

            switch (searchField.getDatatype()) {
                case BOOLEAN:
                    assertDataType(searchField, allValues, Boolean.class);
                    break;
                case DATE:
                    assertDataType(searchField, allValues, LocalDate.class);
                    break;
                case DATETIME:
                    assertDataType(searchField, allValues, LocalDateTime.class);
                    break;
                case NUMBER:
                    assertDataType(searchField, allValues, Number.class);
                    break;
                case TEXT:
                    assertDataType(searchField, allValues, String.class);
                    break;
            }
        }

        private <T> void assertDataType(SearchField searchField, List<Object> allValues, Class<T> type) {
            if (allValues.stream().anyMatch(value -> !(type.isInstance(value)))) {
                throw new SearchConfigRequestException(searchField, searchField.getDatatype().toString(), "value was not of type " + type.getSimpleName());
            }
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
