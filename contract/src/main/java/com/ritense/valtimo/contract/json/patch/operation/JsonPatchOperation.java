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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;

import java.util.Objects;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public abstract class JsonPatchOperation {

    private final Operation operation;
    private final JsonPointer path;

    protected JsonPatchOperation(
        Operation operation,
        JsonPointer path
    ) {
        assertArgumentNotNull(operation, "operation is required");
        assertArgumentNotNull(path, "path is required");
        this.operation = operation;
        this.path = path;
    }

    @JsonProperty("op")
    public String getOperation() {
        return operation.toString().toLowerCase();
    }

    @JsonProperty("path")
    public String getPath() {
        return path.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonPatchOperation)) {
            return false;
        }
        JsonPatchOperation that = (JsonPatchOperation) o;
        return operation == that.operation &&
            path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, path);
    }

}