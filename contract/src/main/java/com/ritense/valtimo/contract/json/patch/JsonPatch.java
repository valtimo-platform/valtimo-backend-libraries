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

package com.ritense.valtimo.contract.json.patch;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.contract.json.Mapper;
import com.ritense.valtimo.contract.json.patch.operation.JsonPatchOperation;

import java.util.LinkedHashSet;

public class JsonPatch {

    @JsonValue
    private final LinkedHashSet<JsonPatchOperation> patches;

    public JsonPatch() {
        this.patches = new LinkedHashSet<>();
    }

    public JsonPatch(LinkedHashSet<JsonPatchOperation> patches) {
        this.patches = patches;
    }

    public void add(JsonPatchOperation operation) {
        patches.add(operation);
    }

    public JsonNode toJson() {
        ObjectMapper mapper = Mapper.INSTANCE.get();
        return mapper.convertValue(this, JsonNode.class);
    }

    public LinkedHashSet<JsonPatchOperation> patches() {
        return patches;
    }

}