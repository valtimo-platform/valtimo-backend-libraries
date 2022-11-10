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
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.MULTIPLE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldFieldtype.RANGE;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchtype.EXACT;
import static com.ritense.document.domain.impl.searchfield.SearchFieldMatchtype.LIKE;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
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
        SearchRequestValidator.validate(searchFilter, searchField);

        var rangeFrom = mapWhenTemporalField(searchFilter.getRangeFromSearchRequestValue());
        var rangeTo = mapWhenTemporalField(searchFilter.getRangeToSearchRequestValue());

        List<SearchRequestValue> searchRequestValues = null;
        if (searchFilter.getSearchRequestValues() != null) {
            searchRequestValues = searchFilter.getSearchRequestValues().stream()
                .map(SearchRequestMapper::mapWhenTemporalField)
                .collect(Collectors.toList());
        }

        var searchCriteria2 = new SearchRequest2.SearchCriteria2();
        searchCriteria2.setPath(searchField.getPath());
        searchCriteria2.setSearchType(findDatabaseSearchType(searchFilter, searchField));
        searchCriteria2.setRangeFrom(rangeFrom.getComparableValue());
        searchCriteria2.setRangeTo(rangeTo.getComparableValue());
        searchCriteria2.setSearchRequestValues(searchRequestValues);
        SearchRequestValidator.validate(searchCriteria2);
        return searchCriteria2;
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
        if (searchField.getFieldtype() == MULTIPLE) {
            return DatabaseSearchType.IN;
        } else if (searchField.getFieldtype() == RANGE) {
            if (searchFilter.getRangeFrom() != null && searchFilter.getRangeTo() != null) {
                return DatabaseSearchType.BETWEEN;
            } else if (searchFilter.getRangeFrom() != null) {
                return DatabaseSearchType.GREATER_THAN_OR_EQUAL_TO;
            } else if (searchFilter.getRangeTo() != null) {
                return DatabaseSearchType.LESS_THAN_OR_EQUAL_TO;
            } else {
                throw new SearchConfigRequestException(searchField, searchField.getFieldtype().toString(), "range parameters were not found");
            }
        } else if (searchField.getMatchtype() == LIKE) {
            return DatabaseSearchType.LIKE;
        } else if (searchField.getMatchtype() == EXACT) {
            return DatabaseSearchType.EQUAL;
        } else {
            throw new IllegalStateException("Unknown match type: " + searchField.getMatchtype());
        }
    }

}
