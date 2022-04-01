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

package com.ritense.processdocument;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.ritense.valtimo.contract.json.JsonPointerHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonTest {
    private String json;

    @BeforeEach
    public void setUp() {
        json = "{\"request\": {}}";
    }

    @Test
    public void shouldCreateJsonFromPath() {
        final DocumentContext put = JsonPath.parse(json).put("$.request", "new-key", "new-value");
        final Object read = put.read("$.request.new-key");
        assertThat(read).isEqualTo("new-value");
    }

    @Test
    public void shouldAddNodeWithValue() {
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        JsonPointerHelper.appendJsonPointerTo(rootNode, JsonPointer.compile("/request/customer/0/firstName"), new TextNode("John"));

        assertThat(rootNode.get("request").get("customer").get(0).get("firstName").textValue()).isEqualTo("John");
    }

    @Test
    public void shouldAddNodeWithValues() {
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        JsonPointerHelper.appendJsonPointerTo(rootNode, JsonPointer.compile("/root/array/0/name"), new TextNode("John"));
        JsonPointerHelper.appendJsonPointerTo(rootNode, JsonPointer.compile("/root/array/0/age"), new IntNode(17));
        JsonPointerHelper.appendJsonPointerTo(rootNode, JsonPointer.compile("/root/array/4"), new IntNode(12));
        JsonPointerHelper.appendJsonPointerTo(rootNode, JsonPointer.compile("/root/object/num"), new IntNode(81));
        JsonPointerHelper.appendJsonPointerTo(rootNode, JsonPointer.compile("/root/object/str"), new TextNode("text"));
        JsonPointerHelper.appendJsonPointerTo(rootNode, JsonPointer.compile("/descr"), new TextNode("description"));

        assertThat(rootNode.get("root").get("array").get(0).get("name").textValue()).isEqualTo("John");
        assertThat(rootNode.get("root").get("array").get(0).get("age").intValue()).isEqualTo(17);
        assertThat(rootNode.get("descr").textValue()).isEqualTo("description");
    }

}
