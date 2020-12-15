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
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPatchBuilderTest {

    @Test
    public void shouldHaveAddPropertiesOnly() {
        JsonPatchBuilder jsonPatchBuilder = new JsonPatchBuilder();

        jsonPatchBuilder.add(JsonPointer.valueOf("/name"), new TextNode("aaa"));

        JsonPatch jsonPatch = jsonPatchBuilder.build();

        JsonNode patch = jsonPatch.toJson();

        assertThat(patch.isArray()).isTrue();
        assertThat(patch.get(0).has("op")).isTrue();
        assertThat(patch.get(0).has("path")).isTrue();
        assertThat(patch.get(0).has("value")).isTrue();
        assertThat(patch.get(0).has("to")).isFalse();

    }
}