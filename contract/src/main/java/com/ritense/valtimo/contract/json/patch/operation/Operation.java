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

package com.ritense.valtimo.contract.json.patch.operation;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Operation {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
    TEST("test");

    private final String name;

    Operation(String name) {
        this.name = name;
    }

    @JsonCreator
    public static Operation fromString(String text) {
        for (Operation operation : Operation.values()) {
            if (operation.name.equalsIgnoreCase(text)) {
                return operation;
            }
        }
        throw new IllegalStateException(String.format("Cannot create Operation from string %s", text));
    }

    public String toString() {
        return this.name;
    }

}
