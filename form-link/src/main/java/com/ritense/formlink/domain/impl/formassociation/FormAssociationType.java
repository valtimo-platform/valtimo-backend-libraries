/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.formlink.domain.impl.formassociation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Deprecated(since = "10.6.0", forRemoval = true)
public enum FormAssociationType {
    @JsonProperty("start-event")
    START_EVENT("start-event"),
    @JsonProperty("user-task")
    USER_TASK("user-task");

    private final String name;

    FormAssociationType(String name) {
        this.name = name;
    }

    @JsonCreator
    public static FormAssociationType fromString(String text) {
        for (FormAssociationType formAssociationType : FormAssociationType.values()) {
            if (formAssociationType.name.equalsIgnoreCase(text)) {
                return formAssociationType;
            }
        }
        throw new IllegalStateException(String.format("Cannot create FormAssociationType from string %s", text));
    }

    public String toString() {
        return this.name;
    }

}
