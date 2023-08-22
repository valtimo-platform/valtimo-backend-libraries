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

package com.ritense.document.domain.patch;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonPatch;

import java.util.EnumSet;

import static com.ritense.document.domain.patch.JsonPatchFilterFlag.defaultPatchFlags;
import static com.ritense.document.domain.patch.JsonPatchFlag.defaultCompatibilityFlags;

public interface JsonPatchService {

    static void apply(JsonNode patch, JsonNode source) {
        apply(patch, source, defaultPatchFlags());
    }

    static void apply(JsonNode patch, JsonNode source, EnumSet<JsonPatchFilterFlag> jsonPatchFilterFlags) {
        JsonPatchFilter.filter(patch, jsonPatchFilterFlags);
        JsonPatch.applyInPlace(patch, source, defaultCompatibilityFlags());
    }

    static void apply(com.ritense.valtimo.contract.json.patch.JsonPatch patch, JsonNode source) {
       apply(patch.toJson(), source);
    }

    static void apply(
        com.ritense.valtimo.contract.json.patch.JsonPatch patch,
        JsonNode source,
        EnumSet<JsonPatchFilterFlag> jsonPatchFilterFlags
    ) {
        apply(patch.toJson(), source, jsonPatchFilterFlags);
    }

}
