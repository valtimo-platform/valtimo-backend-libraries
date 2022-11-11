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

package com.ritense.document.domain.impl.searchfield;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchFieldFieldType {
    MULTIPLE("multiple"),
    RANGE("range"),
    SINGLE("single");

    @JsonValue private final String name;

    SearchFieldFieldType(String name) {
        this.name = name;
    }

    public static SearchFieldFieldType fromString(String text) {
        for (SearchFieldFieldType fieldType : SearchFieldFieldType.values()) {
            if (fieldType.name.equalsIgnoreCase(text)) {
                return fieldType;
            }
        }
        throw new IllegalStateException(String.format("Cannot create SearchFieldFieldType from string %s", text));
    }

    public static SearchFieldFieldType fromKey(String key) {
        for (SearchFieldFieldType fieldType : SearchFieldFieldType.values()) {
            if (fieldType.name().equalsIgnoreCase(key)) {
                return fieldType;
            }
        }
        throw new IllegalStateException(String.format("Cannot create SearchFieldFieldType from string %s", key));
    }

    @Override
    public String toString() {
        return this.name;
    }
}
