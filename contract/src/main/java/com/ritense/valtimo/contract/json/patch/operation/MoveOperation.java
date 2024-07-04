/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonPointer;
import java.util.Objects;

public class MoveOperation extends JsonPatchOperation {

    private final JsonPointer fromPath;

    public MoveOperation(JsonPointer from, JsonPointer to) {
        super(Operation.MOVE, to);
        assertArgumentNotNull(from, "from is required");
        this.fromPath = from;
    }

    @JsonProperty("from")
    public String getFromPath() {
        return fromPath.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoveOperation)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MoveOperation that = (MoveOperation) o;
        return fromPath.equals(that.fromPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fromPath);
    }

}
