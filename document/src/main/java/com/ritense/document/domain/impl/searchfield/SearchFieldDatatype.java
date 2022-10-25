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

public enum SearchFieldDatatype {
    BOOLEAN("boolean"),
    DATE("date"),
    DATETIME("datetime"),
    NUMBER("number"),
    TEXT("text");

    private final String name;

    SearchFieldDatatype(String name) {
        this.name = name;
    }

    public static SearchFieldDatatype fromString(String text) {
        for (SearchFieldDatatype datatype : SearchFieldDatatype.values()) {
            if (datatype.name.equalsIgnoreCase(text)) {
                return datatype;
            }
        }
        throw new IllegalStateException(String.format("Cannot create SearchFieldDatatype from string %s", text));
    }

    public static SearchFieldDatatype fromKey(String key) {
        for (SearchFieldDatatype datatype : SearchFieldDatatype.values()) {
            if (datatype.name().equalsIgnoreCase(key)) {
                return datatype;
            }
        }
        throw new IllegalStateException(String.format("Cannot create SearchFieldDatatype from string %s", key));
    }

    @Override
    public String toString() {
        return this.name;
    }
}
