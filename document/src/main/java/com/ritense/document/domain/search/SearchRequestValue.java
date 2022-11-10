/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1. (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.document.domain.search;

import com.fasterxml.jackson.annotation.JsonValue;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentFalse;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentTrue;

/**
 *
 */
public class SearchRequestValue {

    @JsonValue
    private Object value;

    private SearchRequestValue() {
    }

    public static SearchRequestValue of(Object value) {
        assertArgumentFalse(value instanceof SearchRequestValue, "Value mustn't be of type 'SearchRequestValue'");
        var object = new SearchRequestValue();
        object.setValue(value);
        return object;
    }


    public static SearchRequestValue ofNull() {
        return of(null);
    }

    public static SearchRequestValue ofComparable(Object value) {
        assertArgumentTrue(value == null || value instanceof Comparable, "Value '" + value + "' isn't of type Comparable");
        return of(value);
    }

    public static SearchRequestValue ofTemporal(Temporal value) {
        return ofComparable(value);
    }

    public static List<SearchRequestValue> ofList(Collection<Object> values) {
        if (values == null) {
            return List.of();
        } else {
            return values.stream().map(SearchRequestValue::of).collect(Collectors.toList());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> getValueClass() {
        return (Class<T>) value.getClass();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    public String getValueAsString() {
        return (String) value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<? super T>> T getComparableValue() {
        return (T) value;
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isNotNull() {
        return value != null;
    }

    public boolean isString() {
        return value instanceof String;
    }
}
