/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.form;

/**
 * @deprecated use string instead to indicate external form field type. supports method in {@link FormFieldDataResolver}
 * has been overloaded. use {@link FormFieldDataResolver#supports(String)}
 */
@Deprecated(since = "9.19.0", forRemoval = true)
public enum ExternalFormFieldType {
    OZ("OpenZaak"),
    OA("ObjectsApi");

    private final String name;

    ExternalFormFieldType(String name) {
        this.name = name;
    }

    public static ExternalFormFieldType fromString(String text) {
        for (ExternalFormFieldType externalFormFieldType : ExternalFormFieldType.values()) {
            if (externalFormFieldType.name.equalsIgnoreCase(text)) {
                return externalFormFieldType;
            }
        }
        throw new IllegalStateException(String.format("Cannot create ExternalFormFieldType from string %s", text));
    }

    public static ExternalFormFieldType fromKey(String key) {
        for (ExternalFormFieldType externalFormFieldType : ExternalFormFieldType.values()) {
            if (externalFormFieldType.name().equalsIgnoreCase(key)) {
                return externalFormFieldType;
            }
        }
        throw new IllegalStateException(String.format("Cannot create ExternalFormFieldType from string %s", key));
    }

    public String toString() {
        return this.name;
    }

}