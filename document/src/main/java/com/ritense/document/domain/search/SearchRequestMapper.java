package com.ritense.document.domain.search;

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.exception.SearchConfigRequestException;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.MULTIPLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.RANGE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.SINGLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchtype.EXACT;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchtype.LIKE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_TIME;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

public class SearchRequestMapper {

    private static final List<Pair<DateTimeFormatter, TemporalQuery<Temporal>>> TEMPORAL_MAP = List.of(
        Pair.of(ISO_LOCAL_DATE, LocalDate::from),
        Pair.of(ISO_LOCAL_DATE_TIME, LocalDateTime::from),
        Pair.of(ISO_LOCAL_TIME, LocalTime::from),
        Pair.of(ISO_OFFSET_DATE_TIME, OffsetDateTime::from),
        Pair.of(ISO_OFFSET_TIME, OffsetTime::from),
        Pair.of(ISO_ZONED_DATE_TIME, ZonedDateTime::from)
    );

    private SearchRequestMapper() {
    }

    public static SearchRequest2 toSearchRequest2(SearchWithConfigRequest searchRequest, List<SearchRequest2.SearchCriteria2> otherFilters) {
        var searchRequest2 = new SearchRequest2();
        searchRequest2.setCreatedBy(searchRequest.getCreatedBy());
        searchRequest2.setSequence(searchRequest.getSequence());
        searchRequest2.setSearchOperator(searchRequest.getSearchOperator());
        searchRequest2.setOtherFilters(otherFilters);
        return searchRequest2;
    }

    public static SearchRequest2.SearchCriteria2 toSearchCriteria2(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchField searchField) {
        assertSearchFieldType(searchFilter, searchField);
        assertDataType(searchFilter, searchField);

        var rangeFrom = mapWhenTemporalField(searchFilter.getRangeFrom());
        var rangeTo = mapWhenTemporalField(searchFilter.getRangeTo());

        List<Object> values = null;
        if (searchFilter.getValues() != null) {
            values = searchFilter.getValues().stream()
                .map(SearchRequestMapper::mapWhenTemporalField)
                .collect(Collectors.toList());
        }

        var searchCriteria2 = new SearchRequest2.SearchCriteria2();
        searchCriteria2.setPath(searchField.getPath());
        searchCriteria2.setSearchType(findDatabaseSearchType(searchFilter, searchField));
        searchCriteria2.setRangeFrom(rangeFrom);
        searchCriteria2.setRangeTo(rangeTo);
        searchCriteria2.setValues(values);
        return searchCriteria2;
    }

    private static void assertSearchFieldType(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchField searchField) {
        if (searchField.getFieldtype() == MULTIPLE || searchField.getFieldtype() == SINGLE) {
            if (searchFilter.getRangeFrom() != null || searchFilter.getRangeTo() != null) {
                throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "range parameters were found");
            }
            if (searchFilter.getValues() == null) {
                throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "no searchFilter.getValues() were found");
            }
        }
        if (searchField.getFieldtype() == SINGLE && (searchFilter.getValues() == null || searchFilter.getValues().isEmpty())) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "no value was found");
        }
        if (searchField.getFieldtype() == SINGLE && searchFilter.getValues() != null && searchFilter.getValues().size() >= 2) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "multiple searchFilter.getValues() were found");
        }
        if (searchField.getFieldtype() == RANGE && searchFilter.getValues() != null && !searchFilter.getValues().isEmpty()) {
            throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "individual searchFilter.getValues() were found");
        }
    }

    private static void assertDataType(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchField searchField) {
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
                assertDataType(searchField, allValues, Boolean.class);
                break;
            case DATE:
            case DATETIME:
                assertDataType(searchField, allValues, String.class, TemporalAccessor.class);
                break;
            case NUMBER:
                assertDataType(searchField, allValues, Number.class);
                break;
            case TEXT:
                assertDataType(searchField, allValues, String.class);
                break;
        }
    }

    private static void assertDataType(SearchField searchField, List<Object> allValues, Class<?>... types) {
        for (var value : allValues) {
            if (Arrays.stream(types).noneMatch(type -> type.isInstance(value))) {
                throw new SearchConfigRequestException(searchField, searchField.getDatatype().toString(), "value '" + value + "' was not of type " + Arrays.toString(types));
            }
        }
    }

    private static Object mapWhenTemporalField(Object field) {
        if (field == null) {
            return null;
        } else if (field instanceof String) {
            for (var pair : TEMPORAL_MAP) {
                var temporal = parseTemporal(pair.getFirst(), (String) field, pair.getSecond());
                if (temporal.isPresent()) {
                    return temporal.get();
                }
            }
            return field;
        } else {
            return field;
        }
    }

    private static Optional<Temporal> parseTemporal(DateTimeFormatter formatter, String field, TemporalQuery<Temporal> query) {
        try {
            return Optional.of(formatter.parse(field, query));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    private static DatabaseSearchType findDatabaseSearchType(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchField searchField) {
        if (searchField.getFieldtype() == MULTIPLE) {
            return DatabaseSearchType.IN;
        } else if (searchField.getFieldtype() == RANGE) {
            if (searchFilter.getRangeFrom() != null && searchFilter.getRangeTo() != null) {
                return DatabaseSearchType.BETWEEN;
            } else if (searchFilter.getRangeFrom() != null) {
                return DatabaseSearchType.FROM;
            } else if (searchFilter.getRangeTo() != null) {
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

}
