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

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.valtimo.contract.json.patch.operation.AddOperation;
import com.ritense.valtimo.contract.json.patch.operation.CopyOperation;
import com.ritense.valtimo.contract.json.patch.operation.JsonPatchOperation;
import com.ritense.valtimo.contract.json.patch.operation.MoveOperation;
import com.ritense.valtimo.contract.json.patch.operation.RemoveOperation;
import com.ritense.valtimo.contract.json.patch.operation.ReplaceOperation;

import java.util.LinkedHashSet;

import static com.fasterxml.jackson.module.kotlin.ExtensionsKt.jacksonObjectMapper;

/**
 * A builder for constructing a JSON Patch by adding
 * JSON Patch operations incrementally.
 * <p>
 * The following illustrates the approach.
 * <pre>
 *   JsonPatchBuilder builder = new JsonPatchBuilder();
 *   JsonPatch patch = builder.add("/John/phones/office", "1234-567")
 *                            .remove("/Amy/age")
 *                            .build();
 * </pre>
 * The result is equivalent to the following JSON Patch.
 * <pre>
 * [
 *    {"op" = "add", "path" = "/John/phones/office", "value" = "1234-567"},
 *    {"op" = "remove", "path" = "/Amy/age"}
 * ] </pre>
 */
public final class JsonPatchBuilder {

    private final LinkedHashSet<JsonPatchOperation> operations;

    public JsonPatchBuilder(LinkedHashSet<JsonPatchOperation> operations) {
        this.operations = operations;
    }

    public JsonPatchBuilder() {
        this.operations = new LinkedHashSet<>();
    }

    public JsonPatchBuilder add(JsonPointer path, JsonNode value) {
        operations.add(new AddOperation(path, value));
        return this;
    }

    public JsonPatchBuilder remove(JsonPointer path) {
        operations.add(new RemoveOperation(path));
        return this;
    }

    public JsonPatchBuilder replace(JsonPointer path, JsonNode value) {
        operations.add(new ReplaceOperation(path, value));
        return this;
    }

    public JsonPatchBuilder move(JsonPointer from, JsonPointer to) {
        operations.add(new MoveOperation(from, to));
        return this;
    }

    public JsonPatchBuilder copy(JsonPointer from, JsonPointer to) {
        operations.add(new CopyOperation(from, to));
        return this;
    }

    /** Adds a JsonNode value to a json at the specified location. */
    public JsonPatchBuilder addJsonNodeValue(JsonNode destination, JsonPointer path, JsonNode value) {
        if (destination.at(path.head()).isMissingNode()) {
            var propertyName = path.last().getMatchingProperty();
            JsonNode newValue;
            if ("-".equals(propertyName) || "0".equals(propertyName))
                newValue = jacksonObjectMapper().createArrayNode().add(value);
            else
                newValue = jacksonObjectMapper().createObjectNode().set(path.last().getMatchingProperty(), value);

            addJsonNodeValue(destination, path.head(), newValue);
        } else {
            var currentValue = destination.at(path);
            if (currentValue.isMissingNode() || currentValue.isArray()) {
                add(path, value);
            } else {
                replace(path, value);
            }
        }
        return this;
    }

    public JsonPatch build() {
        return new JsonPatch(operations);
    }

}
