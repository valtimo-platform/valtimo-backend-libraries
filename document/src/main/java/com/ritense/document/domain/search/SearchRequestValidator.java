package com.ritense.document.domain.search;

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.exception.SearchConfigRequestException;

import javax.validation.ValidationException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.MULTIPLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.RANGE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.SINGLE;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

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

    public static void validate(SearchWithConfigRequest searchRequest) {
        if (searchRequest.getOtherFilters() != null) {
            if (searchRequest.getSearchOperator() == null) {
                throw new ValidationException("SearchOperator not present");
            }
            searchRequest.getOtherFilters().forEach(SearchRequestValidator::validate);
        }
    }

    public static void validate(SearchRequest2.SearchCriteria2 searchCriteria) {
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

    public static void validate(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchField searchField) {
        validateSearchFieldType(searchFilter, searchField);
        validateDataType(searchFilter, searchField);
    }

    private static void validate(SearchWithConfigRequest.SearchWithConfigFilter otherFilter) {
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

    private static void validateSearchFieldType(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchField searchField) {
        if ((searchField.getFieldtype() == MULTIPLE || searchField.getFieldtype() == SINGLE)
            && (searchFilter.getRangeFrom() != null || searchFilter.getRangeTo() != null)) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "range parameters were found");
        }
        if (searchField.getFieldtype() == SINGLE && searchFilter.getValues().isEmpty()) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "no value was found");
        }
        if (searchField.getFieldtype() == SINGLE && searchFilter.getValues().size() >= 2) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "multiple values were found");
        }
        if (searchField.getFieldtype() == RANGE && !searchFilter.getValues().isEmpty()) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "individual values were found");
        }
    }

    private static void validateDataType(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchField searchField) {
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

        switch (searchField.getDatatype()) {
            case BOOLEAN:
                validateDataType(searchField, allValues, Boolean.class);
                break;
            case DATE:
                validateDataType(searchField, allValues, String.class, TemporalAccessor.class);
                validateDateTimeFormatIfString(allValues, DATE_FORMATTERS);
                break;
            case DATETIME:
                validateDataType(searchField, allValues, String.class, TemporalAccessor.class);
                validateDateTimeFormatIfString(allValues, DATE_TIME_FORMATTERS);
                break;
            case NUMBER:
                validateDataType(searchField, allValues, Number.class);
                break;
            case TEXT:
                validateDataType(searchField, allValues, String.class);
                break;
        }
    }

    private static void validateDateTimeFormatIfString(List<?> allValues, List<DateTimeFormatter> dateTimeFormatters) {
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
                    throw new ValidationException("Search value '" + Arrays.toString(allValues.toArray()) + "' doesn't have a date format");
                }
            }
        }
    }


    private static void validateDataType(SearchField searchField, List<?> allValues, Class<?>... types) {
        if (Arrays.stream(types).noneMatch(type -> hasType(allValues, type))) {
            throw new SearchConfigRequestException(searchField, searchField.getDatatype().toString(), "values '" + Arrays.toString(allValues.toArray()) + "' was not of type " + Arrays.toString(types));
        }
    }


    private static boolean hasType(List<?> allValues, Class<?> type) {
        return allValues.stream().allMatch(type::isInstance);
    }

}
