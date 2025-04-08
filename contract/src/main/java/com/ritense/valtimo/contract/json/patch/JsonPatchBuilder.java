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
     * This will adjust the first un-indexed array position ('/-' or '/+') to an index depending on previous operations or destination object.
     * The '/-' will add the JSON to a new element in the array.
     * The '/+' will group JSON operations together and will add this group to a single new element in the array.
     * Any subsequent occurrences of '/-' will be replaced by 0, as the first one will create a new node already.
     * <p>Example (where x in destination has a length of 1):
     * /x/-/y/-/z -> /x/1/y/0/z</p>
     */
    private JsonPointer determineUnindexedPath(JsonNode destination, JsonPointer path) {
        String stringPath = path.toString();
        int dashIndex = stringPath.indexOf("/-");
        int plusIndex = stringPath.indexOf("/+");
        if (dashIndex == -1 && plusIndex == -1) {
            return path;
        } else if (dashIndex != -1) {
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
        } else {
            String arrayPath = stringPath.substring(0, plusIndex);
            boolean prevOpIsAppendable = false;
            for (int i = 0; ; i++) {
                String testPath = arrayPath + "/" + i;
                var op = operations.stream().filter(it -> it.getPath().equals(testPath)).findFirst().orElse(null);
                if (op == null && destination.at(testPath).isMissingNode()) {
                    int appendI = prevOpIsAppendable ? i - 1 : i;
                    String correctedPath = arrayPath + "/" + appendI + stringPath.substring(plusIndex + 2);
                    return determineUnindexedPath(destination, JsonPointer.compile(correctedPath));
                }
                prevOpIsAppendable = op != null && op instanceof AddOperation addOp && addOp.getValue().isObject();
            }
        }
    }

    private void addJsonNodeValueInternal(JsonNode destination, JsonPointer path, JsonNode value) {
        var head = destination.at(path.head());
        if (!head.isObject() && !head.isArray()) {
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

}
