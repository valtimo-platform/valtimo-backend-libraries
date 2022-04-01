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

public class CopyOperation extends JsonPatchOperation {

    private final JsonPointer toPath; //only to be used in move/copy operation

    public CopyOperation(JsonPointer from, JsonPointer to) {
        super(Operation.COPY, from);
        assertArgumentNotNull(to, "to is required");
        this.toPath = to;
    }

    @JsonProperty("to")
    public String getToPath() {
        return toPath.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyOperation)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        CopyOperation that = (CopyOperation) o;
        return toPath.equals(that.toPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), toPath);
    }

}