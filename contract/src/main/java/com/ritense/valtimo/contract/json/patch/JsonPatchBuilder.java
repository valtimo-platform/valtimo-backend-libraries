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

package com.ritense.valtimo.contract.json.patch;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.ritense.valtimo.contract.json.MapperSingleton;
import com.ritense.valtimo.contract.json.patch.operation.AddOperation;
import com.ritense.valtimo.contract.json.patch.operation.CopyOperation;
import com.ritense.valtimo.contract.json.patch.operation.JsonPatchOperation;
import com.ritense.valtimo.contract.json.patch.operation.MoveOperation;
import com.ritense.valtimo.contract.json.patch.operation.RemoveOperation;
import com.ritense.valtimo.contract.json.patch.operation.ReplaceOperation;
import java.util.LinkedHashSet;

/**
 * A builder for constructing a JSON Patch by adding
 * JSON Patch operations incrementally.
 *
 * <p>The following illustrates the approach.</p>
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

    /**
     * Adds a JsonNode value to a json at the specified location.
     */
    public JsonPatchBuilder addJsonNodeValue(JsonNode destination, JsonPointer path, JsonNode value) {
        JsonPointer workPath = determineUnindexedPath(destination, path);
        addJsonNodeValueInternal(destination, workPath, value);

        return this;
    }

    /**
     * This will adjust the first unindexed array position ('/-') to a new index depending on previous operations or destination object.
     * Any subsequent occurrences of '/-' will be replaced by 0, as the first one will create a new node already.
     *
     * <p>Example (where x in destination has a length of 1):
     * /x/-/y/-/z -> /x/1/y/0/z</p>
     */
    private JsonPointer determineUnindexedPath(JsonNode destination, JsonPointer path) {
        String stringPath = path.toString();
        int dashIndex = stringPath.indexOf("/-");
        if (dashIndex == -1) {
            return path;
        }

        String arrayPath = stringPath.substring(0, dashIndex);
        for (int i = 0; ; i++) {
            String testPath = arrayPath + "/" + i;
            if (operations.stream().noneMatch(op -> op.getPath().equals(testPath))
                && destination.at(testPath).isMissingNode()
            ) {
                String correctedPath = testPath + stringPath.substring(dashIndex + 2)
                    .replace("/-", "/0");
                return JsonPointer.compile(correctedPath);
            }
        }
    }

    private void addJsonNodeValueInternal(JsonNode destination, JsonPointer path, JsonNode value) {
        if (get(destination, path.head()).isMissingNode()) {
            var propertyName = path.last().getMatchingProperty();
            JsonNode newValue;
            if (propertyName.matches("\\d+")) {
                newValue = MapperSingleton.INSTANCE.get().createArrayNode();
            } else {
                newValue = MapperSingleton.INSTANCE.get().createObjectNode();
            }

            addJsonNodeValueInternal(destination, path.head(), newValue);
        }

        var currentValue = destination.at(path);
        if (currentValue.isMissingNode() || currentValue.isArray()) {
            add(path, value);
        } else {
            replace(path, value);
        }
    }

    public JsonPatch build() {
        return new JsonPatch(operations);
    }

    private JsonNode get(JsonNode json, JsonPointer path) {
        for (var op : operations) {
            if (op instanceof AddOperation addOp && op.getPath().equals(path.toString())) {
                return addOp.getValue();
            } else if (op instanceof ReplaceOperation repOp && op.getPath().equals(path.toString())) {
                return repOp.getValue();
            } else if (op instanceof RemoveOperation && op.getPath().equals(path.toString())) {
                return MissingNode.getInstance();
            } else if (op instanceof MoveOperation moveOp && moveOp.getToPath().equals(path.toString())) {
                return get(json, JsonPointer.valueOf(moveOp.getPath()));
            } else if (op instanceof CopyOperation copyOp && copyOp.getToPath().equals(path.toString())) {
                return get(json, JsonPointer.valueOf(copyOp.getPath()));
            }
        }

        return json.at(path);
    }

}
