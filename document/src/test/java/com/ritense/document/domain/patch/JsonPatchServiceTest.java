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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonPatchServiceTest {

    private ObjectNode source;

    @BeforeEach
    void setUp() {
        source = JsonNodeFactory.instance.objectNode();
        ArrayNode favorites = JsonNodeFactory.instance.arrayNode();
        favorites.add("Croissant");
        source.set("favorites", favorites);
    }

    @Test
    public void addArrayItemToEndOfArray() {

        final ObjectNode addOperation = JsonNodeFactory.instance.objectNode();
        addOperation.put("op", "add");
        addOperation.put("path", "/favorites/-");
        addOperation.put("value", "Bread");

        ArrayNode patches = JsonNodeFactory.instance.arrayNode();
        patches.add(addOperation);

        //when
        JsonPatchService.apply(patches, source);

        //then
        assertThat(source.at("/favorites/0").textValue()).isEqualTo("Croissant");
        assertThat(source.at("/favorites/1").textValue()).isEqualTo("Bread");
    }

    @Test
    public void removeArrayItem() {

        final ObjectNode addOperation = JsonNodeFactory.instance.objectNode();
        addOperation.put("op", "add");
        addOperation.put("path", "/favorites/-");
        addOperation.put("value", "Bread");

        ArrayNode patches = JsonNodeFactory.instance.arrayNode();
        patches.add(addOperation);

        //when
        JsonPatchService.apply(patches, source);

        //then
        assertThat(source.at("/favorites/0").textValue()).isEqualTo("Croissant");
        assertThat(source.at("/favorites/1").textValue()).isEqualTo("Bread");
    }

    @Test
    public void addArrayItemToEndOfArrayCombined() {

        ObjectNode source = JsonNodeFactory.instance.objectNode();
        ArrayNode favorites = JsonNodeFactory.instance.arrayNode();

        ObjectNode bread = JsonNodeFactory.instance.objectNode();
        bread.put("name", "aName");
        bread.put("size", "aSize");

        //favorites.add(bread);
        source.set("favorites", favorites);


        final ObjectNode arrayOperation = JsonNodeFactory.instance.objectNode();
        arrayOperation.put("op", "add");
        arrayOperation.put("path", "/favorites/0");
        arrayOperation.set("value", JsonNodeFactory.instance.objectNode());


        final ObjectNode addOperation = JsonNodeFactory.instance.objectNode();
        addOperation.put("op", "add");
        addOperation.put("path", "/favorites/0/name");
        addOperation.put("value", "Pita");

        final ObjectNode addOperation2 = JsonNodeFactory.instance.objectNode();
        addOperation2.put("op", "add");
        addOperation2.put("path", "/favorites/0/size");
        addOperation2.put("value", "med");

        ArrayNode patches = JsonNodeFactory.instance.arrayNode();
        patches.add(arrayOperation);
        patches.add(addOperation);
        patches.add(addOperation2);

        //when
        JsonPatchService.apply(patches, source);

        //then
        assertThat(source.at("/favorites/0/name").textValue()).isEqualTo("Pita");
        assertThat(source.at("/favorites/0/size").textValue()).isEqualTo("med");
    }

    @Test
    public void addArrayItemOnIndex() {

        final ObjectNode addOperation = JsonNodeFactory.instance.objectNode();
        addOperation.put("op", "add");
        addOperation.put("path", "/favorites/1");
        addOperation.put("value", "Bread");

        ArrayNode patches = JsonNodeFactory.instance.arrayNode();
        patches.add(addOperation);

        //when
        JsonPatchService.apply(patches, source);

        //then
        assertThat(source.at("/favorites/0").textValue()).isEqualTo("Croissant");
        assertThat(source.at("/favorites/1").textValue()).isEqualTo("Bread");
    }

    @Test
    public void addArrayItemToEmptySource() {

        ObjectNode source = JsonNodeFactory.instance.objectNode();

        ArrayNode patches = JsonNodeFactory.instance.arrayNode();

        final ObjectNode addArrayOperation = JsonNodeFactory.instance.objectNode();
        addArrayOperation.put("op", "add");
        addArrayOperation.put("path", "/favorites");
        addArrayOperation.put("value", JsonNodeFactory.instance.arrayNode());
        patches.add(addArrayOperation);

        final ObjectNode addOperation = JsonNodeFactory.instance.objectNode();
        addOperation.put("op", "add");
        addOperation.put("path", "/favorites/0");
        addOperation.put("value", "Bread");

        patches.add(addOperation);

        //when
        JsonPatchService.apply(patches, source);

        //then
        assertThat(source.at("/favorites/0").textValue()).isEqualTo("Bread");
    }

    @Test
    public void replaceArrayItem() {

        final ObjectNode addOperation = JsonNodeFactory.instance.objectNode();
        addOperation.put("op", "replace");
        addOperation.put("path", "/favorites/0");
        addOperation.put("value", "Bread");

        ArrayNode patches = JsonNodeFactory.instance.arrayNode();
        patches.add(addOperation);

        //when
        JsonPatchService.apply(patches, source);

        //then
        assertThat(source.at("/favorites/0").textValue()).isEqualTo("Bread");

    }

    @Test
    public void aaa() {
        ObjectNode source = JsonNodeFactory.instance.objectNode();
        ArrayNode favorites = JsonNodeFactory.instance.arrayNode();
        source.set("favorites", favorites);

        ArrayNode patches = JsonNodeFactory.instance.arrayNode();

        final ObjectNode breadObject = JsonNodeFactory.instance.objectNode();
        breadObject.put("id", "1");
        breadObject.put("name", "Bread");

        final ObjectNode breadAddOperation = JsonNodeFactory.instance.objectNode();
        breadAddOperation.put("op", "add");
        breadAddOperation.put("path", "/favorites/0");
        breadAddOperation.set("value", breadObject);

        patches.add(breadAddOperation);

        //croissant
        final ObjectNode croissantObject = JsonNodeFactory.instance.objectNode();
        croissantObject.put("id", "2");
        croissantObject.put("name", "Croissant");

        final ObjectNode croissantAddOperation = JsonNodeFactory.instance.objectNode();
        croissantAddOperation.put("op", "add");
        croissantAddOperation.put("path", "/favorites/1");
        croissantAddOperation.set("value", croissantObject);


        patches.add(croissantAddOperation);

        //when
        JsonPatchService.apply(patches, source);

        //then
        Object read = JsonPath.read(source.toString(), "$.favorites[?(@.id == '1')]");

        assertThat(source.at("/favorites/0")).isEqualTo(breadObject);

    }

}