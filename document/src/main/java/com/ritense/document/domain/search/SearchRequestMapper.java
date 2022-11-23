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

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.exception.SearchConfigRequestException;
import org.springframework.data.util.Pair;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldType.MULTIPLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldType.RANGE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchType.EXACT;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchType.LIKE;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

public class SearchRequestMapper {

    private static final List<Pair<DateTimeFormatter, TemporalQuery<Temporal>>> TEMPORAL_MAP = List.of(
        Pair.of(ISO_INSTANT, Instant::from),
        Pair.of(ISO_LOCAL_DATE, LocalDate::from),
        Pair.of(ISO_LOCAL_DATE_TIME, LocalDateTime::from),
        Pair.of(ISO_OFFSET_DATE, OffsetDateTime::from),
        Pair.of(ISO_OFFSET_DATE_TIME, OffsetDateTime::from),
        Pair.of(ISO_ZONED_DATE_TIME, ZonedDateTime::from),
        Pair.of(ISO_LOCAL_TIME, LocalTime::from)
    );

    private SearchRequestMapper() {
    }

    public static AdvancedSearchRequest toAdvancedSearchRequest(SearchWithConfigRequest searchRequest, List<AdvancedSearchRequest.OtherFilter> otherFilters) {
        var advancedSearchRequest = new AdvancedSearchRequest();
        advancedSearchRequest.setSearchOperator(searchRequest.getSearchOperator());
        advancedSearchRequest.setAssigneeFilter(searchRequest.getAssigneeFilter());
        advancedSearchRequest.setOtherFilters(otherFilters);
        return advancedSearchRequest;
    }

    public static AdvancedSearchRequest.OtherFilter toOtherFilter(SearchWithConfigRequest.SearchWithConfigFilter searchFilter, SearchField searchField) {
        SearchRequestValidator.validate(searchFilter, searchField);

        var rangeFrom = mapWhenTemporalField(searchFilter.getRangeFromSearchRequestValue());
        var rangeTo = mapWhenTemporalField(searchFilter.getRangeToSearchRequestValue());

        List<SearchRequestValue> searchRequestValues = null;
        if (searchFilter.getSearchRequestValues() != null) {
            searchRequestValues = searchFilter.getSearchRequestValues().stream()
                .map(SearchRequestMapper::mapWhenTemporalField)
                .collect(Collectors.toList());
        }

        var otherFilter = new AdvancedSearchRequest.OtherFilter();
        otherFilter.setPath(searchField.getPath());
        otherFilter.setSearchType(findDatabaseSearchType(searchFilter, searchField));
        otherFilter.setRangeFrom(rangeFrom.getComparableValue());
        otherFilter.setRangeTo(rangeTo.getComparableValue());
        otherFilter.setSearchRequestValues(searchRequestValues);
        SearchRequestValidator.validate(otherFilter);
        return otherFilter;
    }

    private static SearchRequestValue mapWhenTemporalField(SearchRequestValue field) {
        if (field == null) {
            return SearchRequestValue.ofNull();
        } else if (field.isString()) {
            for (var pair : TEMPORAL_MAP) {
                var temporal = parseTemporal(pair.getFirst(), field.getValueAsString(), pair.getSecond());
                if (temporal.isPresent()) {
                    return SearchRequestValue.ofTemporal(temporal.get());
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
        if (searchField.getFieldType() == MULTIPLE) {
            return DatabaseSearchType.IN;
        } else if (searchField.getFieldType() == RANGE) {
            if (searchFilter.getRangeFrom() != null && searchFilter.getRangeTo() != null) {
                return DatabaseSearchType.BETWEEN;
            } else if (searchFilter.getRangeFrom() != null) {
                return DatabaseSearchType.GREATER_THAN_OR_EQUAL_TO;
            } else if (searchFilter.getRangeTo() != null) {
                return DatabaseSearchType.LESS_THAN_OR_EQUAL_TO;
            } else {
                throw new SearchConfigRequestException(searchField, searchField.getFieldType().toString(), "range parameters were not found");
            }
        } else if (searchField.getMatchType() == LIKE) {
            return DatabaseSearchType.LIKE;
        } else if (searchField.getMatchType() == EXACT) {
            return DatabaseSearchType.EQUAL;
        } else {
            throw new IllegalStateException("Unknown match type: " + searchField.getMatchType());
        }
    }

}
