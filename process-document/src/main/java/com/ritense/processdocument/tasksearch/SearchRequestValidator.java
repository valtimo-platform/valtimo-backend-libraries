/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.tasksearch;

import static com.ritense.valtimo.service.CamundaTaskService.TaskFilter.MINE;
import static com.ritense.valtimo.service.CamundaTaskService.TaskFilter.OPEN;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

import com.ritense.processdocument.tasksearch.SearchWithConfigRequest.SearchWithConfigFilter;
import com.ritense.search.domain.DataType;
import com.ritense.search.domain.FieldType;
import com.ritense.search.domain.SearchFieldV2;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import com.ritense.valtimo.service.CamundaTaskService.TaskFilter;
import jakarta.validation.ValidationException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchRequestValidator {

    private SearchRequestValidator() {
    }

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
        ISO_INSTANT,
        ISO_LOCAL_DATE_TIME,
        ISO_OFFSET_DATE_TIME,
        ISO_ZONED_DATE_TIME
    );

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
        ISO_LOCAL_DATE,
        ISO_OFFSET_DATE
    );

    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
        ISO_LOCAL_TIME
    );

    public static void validate(SearchWithConfigRequest searchRequest) {
        validateAssigneeFilter(searchRequest.getAssigneeFilter());
        if (searchRequest.getOtherFilters() != null) {
            if (searchRequest.getSearchOperator() == null) {
                throw new ValidationException("SearchOperator not present");
            }
            searchRequest.getOtherFilters().forEach(SearchRequestValidator::validate);
        }
    }

    public static void validate(AdvancedSearchRequest searchRequest) {
        validateAssigneeFilter(searchRequest.getAssigneeFilter());
        if (searchRequest.getOtherFilters() != null) {
            if (searchRequest.getSearchOperator() == null) {
                throw new ValidationException("SearchOperator not present");
            }
            searchRequest.getOtherFilters().forEach(SearchRequestValidator::validate);
        }
    }

    private static void validateAssigneeFilter(TaskFilter assigneeFilter) {
        if (assigneeFilter == OPEN || assigneeFilter == MINE) {
            var userId = SecurityUtils.getCurrentUserLogin();
            if (userId == null) {
                throw new ValidationException("Failed to search for " + assigneeFilter + ". Reason: User is not logged in.");
            }
        }
    }

    public static void validate(AdvancedSearchRequest.OtherFilter searchCriteria) {
        switch (searchCriteria.getSearchType()) {
            case LIKE:
                validateLike(searchCriteria.getValues(), searchCriteria.getRangeFrom(), searchCriteria.getRangeTo());
                break;
            case EQUAL:
                validateEqual(searchCriteria.getValues(), searchCriteria.getRangeFrom(), searchCriteria.getRangeTo());
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                validateGreaterThanOrEqualTo(searchCriteria.getValues(), searchCriteria.getRangeFrom(), searchCriteria.getRangeTo());
                break;
            case LESS_THAN_OR_EQUAL_TO:
                validateLessThanOrEqualTo(searchCriteria.getValues(), searchCriteria.getRangeFrom(), searchCriteria.getRangeTo());
                break;
            case BETWEEN:
                validateBetween(searchCriteria.getValues(), searchCriteria.getRangeFrom(), searchCriteria.getRangeTo());
                break;
            case IN:
                validateIn(searchCriteria.getValues(), searchCriteria.getRangeFrom(), searchCriteria.getRangeTo());
                break;
        }
    }

    public static void validate(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchFieldV2 searchField) {
        if (searchField == null) {
            throw new ValidationException("No search field configuration found with key '" + searchFilter.getKey() + "'");
        }
        validateSearchFieldType(searchFilter, searchField);
        validateDataType(searchFilter, searchField);
    }

    private static void validate(SearchWithConfigFilter otherFilter) {
        if (otherFilter.getKey() == null) {
            throw new ValidationException("Failed to search. Reason: key is null");
        }
        if (otherFilter.getRangeFrom() != null && otherFilter.getRangeTo() != null
            && otherFilter.getRangeTo().compareTo(otherFilter.getRangeFrom()) < 0) {
            throw new ValidationException("Failed to search. Reason: rangeTo is smaller that rangeFrom");
        }
    }

    private static <T> void validateLike(List<Object> values, Comparable<T> rangeFrom, Comparable<T> rangeTo) {
        if (values == null || values.isEmpty()) {
            throw new ValidationException("Failed to do LIKE search. Reason: no values present");
        } else if (values.stream().noneMatch(String.class::isInstance)) {
            throw new ValidationException("Failed to do LIKE search. Reason: values not of type 'String'");
        } else if (rangeFrom != null || rangeTo != null) {
            throw new ValidationException("Failed to do LIKE search. Reason: range values found");
        }
    }

    private static <T> void validateEqual(List<Object> values, Comparable<T> rangeFrom, Comparable<T> rangeTo) {
        if (values == null || values.isEmpty()) {
            throw new ValidationException("Failed to do EQUAL search. Reason: no values present");
        } else if (rangeFrom != null || rangeTo != null) {
            throw new ValidationException("Failed to do EQUAL search. Reason: range values found");
        }
    }

    private static <T> void validateGreaterThanOrEqualTo(List<Object> values, Comparable<T> rangeFrom, Comparable<T> rangeTo) {
        if (rangeFrom == null) {
            throw new ValidationException("Failed to do GREATER_THAN_OR_EQUAL_TO search. Reason: rangeFrom not present");
        } else if (rangeTo != null) {
            throw new ValidationException("Failed to do GREATER_THAN_OR_EQUAL_TO search. Reason: rangeTo is present");
        } else if (values != null && !values.isEmpty()) {
            throw new ValidationException("Failed to do GREATER_THAN_OR_EQUAL_TO search. Reason: non-range values were found");
        }
    }

    private static <T> void validateLessThanOrEqualTo(List<Object> values, Comparable<T> rangeFrom, Comparable<T> rangeTo) {
        if (rangeTo == null) {
            throw new ValidationException("Failed to do LESS_THAN_OR_EQUAL_TO search. Reason: rangeTo not present");
        } else if (rangeFrom != null) {
            throw new ValidationException("Failed to do LESS_THAN_OR_EQUAL_TO search. Reason: rangeFrom is present");
        } else if (values != null && !values.isEmpty()) {
            throw new ValidationException("Failed to do LESS_THAN_OR_EQUAL_TO search. Reason: non-range values were found");
        }
    }

    private static <T> void validateBetween(List<Object> values, Comparable<T> rangeFrom, Comparable<T> rangeTo) {
        if (rangeTo == null) {
            throw new ValidationException("Failed to do BETWEEN search. Reason: rangeTo not present");
        } else if (rangeFrom == null) {
            throw new ValidationException("Failed to do BETWEEN search. Reason: rangeFrom not present");
        } else if (values != null && !values.isEmpty()) {
            throw new ValidationException("Failed to do BETWEEN search. Reason: non-range values were found");
        }
    }

    private static <T> void validateIn(List<Object> values, Comparable<T> rangeFrom, Comparable<T> rangeTo) {
        if (values == null) {
            throw new ValidationException("Failed to do IN search. Reason: no values present");
        } else if (rangeFrom != null || rangeTo != null) {
            throw new ValidationException("Failed to do IN search. Reason: range values found");
        }
    }

    private static void validateSearchFieldType(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchFieldV2 searchField) {
        if ((searchField.getFieldType() == FieldType.MULTI_SELECT_DROPDOWN
            || searchField.getFieldType() == FieldType.SINGLE
            || searchField.getFieldType() == FieldType.SINGLE_SELECT_DROPDOWN)
            && (searchFilter.getRangeFrom() != null || searchFilter.getRangeTo() != null)) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldType().toString(), "range parameters were found");
        }
        if ((searchField.getFieldType() == FieldType.SINGLE || searchField.getFieldType() == FieldType.SINGLE_SELECT_DROPDOWN) && searchFilter.getValues().isEmpty()) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldType().toString(), "no value was found");
        }
        if ((searchField.getFieldType() == FieldType.SINGLE || searchField.getFieldType() == FieldType.SINGLE_SELECT_DROPDOWN) && searchFilter.getValues().size() >= 2) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldType().toString(), "multiple values were found");
        }
        if (searchField.getFieldType() == FieldType.RANGE && !searchFilter.getValues().isEmpty()) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldType().toString(), "individual values were found");
        }
    }

    private static void validateDataType(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchFieldV2 searchField) {
        var allValues = new ArrayList<>();
        if (searchFilter.getValues() != null) {
            allValues.addAll(searchFilter.getValues());
        }
        if (searchFilter.getRangeFrom() != null) {
            allValues.add(searchFilter.getRangeFrom());
        }
        if (searchFilter.getRangeTo() != null) {
            allValues.add(searchFilter.getRangeTo());
        }

        switch (searchField.getDataType()) {
            case BOOLEAN:
                validateDataType(searchField, allValues, Boolean.class);
                break;
            case DATE:
                validateDataType(searchField, allValues, String.class, TemporalAccessor.class);
                validateDateTimeFormatIfString(allValues, DATE_FORMATTERS, DataType.DATE);
                break;
            case DATETIME:
                validateDataType(searchField, allValues, String.class, TemporalAccessor.class);
                validateDateTimeFormatIfString(allValues, DATE_TIME_FORMATTERS, DataType.DATETIME);
                break;
            case TIME:
                validateDataType(searchField, allValues, String.class, TemporalAccessor.class);
                validateDateTimeFormatIfString(allValues, TIME_FORMATTERS, DataType.TIME);
                break;
            case NUMBER:
                validateDataType(searchField, allValues, Number.class);
                break;
            case TEXT:
                validateDataType(searchField, allValues, String.class);
                break;
        }
    }

    private static void validateDateTimeFormatIfString(List<?> allValues, List<DateTimeFormatter> dateTimeFormatters, DataType dataType) {
        if (hasType(allValues, String.class)) {
            for (var value : allValues) {
                boolean hasDateFormat = false;
                for (var dateTimeFormatter : dateTimeFormatters) {
                    try {
                        dateTimeFormatter.parse((String) value);
                        hasDateFormat = true;
                        break;
                    } catch (DateTimeParseException ignored) {
                        // ignored
                    }
                }
                if (!hasDateFormat) {
                    throw new ValidationException(
                        "Search values '" + Arrays.toString(allValues.toArray()) + "' don't have the correct '" + dataType + "' format"
                    );
                }
            }
        }
    }


    private static void validateDataType(SearchFieldV2 searchField, List<?> allValues, Class<?>... types) {
        if (Arrays.stream(types).noneMatch(type -> hasType(allValues, type))) {
            throw new SearchConfigRequestException(
                searchField,
                searchField.getDataType().toString(),
                "values '" + Arrays.toString(allValues.toArray()) + "' was not of type " + Arrays.toString(types)
            );
        }
    }


    private static boolean hasType(List<?> allValues, Class<?> type) {
        return allValues.stream().allMatch(type::isInstance);
    }
}
