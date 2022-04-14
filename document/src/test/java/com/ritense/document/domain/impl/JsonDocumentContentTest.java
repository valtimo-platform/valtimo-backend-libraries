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

package com.ritense.document.domain.impl;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JsonDocumentContentTest {

    private ObjectNode source;

    @BeforeEach
    void setUp() {
        source = JsonNodeFactory.instance.objectNode();

        ArrayNode favorites = JsonNodeFactory.instance.arrayNode();
        ObjectNode identity = JsonNodeFactory.instance.objectNode();
        identity.put("age", 32);
        identity.put("name", "Julia");

        ObjectNode bread = JsonNodeFactory.instance.objectNode();
        bread.put("name", "bread");
        bread.put("size", "small");

        ObjectNode croissaint = JsonNodeFactory.instance.objectNode();
        croissaint.put("name", "croissaint");
        croissaint.put("size", "large");

        favorites.add(bread);
        favorites.add(croissaint);

        source.set("identity", identity);
        source.set("favorites", favorites);
    }

    @Test
    void shouldRemoveArrayItemOnlyWithBuild() {
        ObjectNode modifiedContent = JsonNodeFactory.instance.objectNode();
        ArrayNode newFavorite = JsonNodeFactory.instance.arrayNode();

        ObjectNode painAuxChocolat = JsonNodeFactory.instance.objectNode();
        painAuxChocolat.put("name", "painAuxChocolat");
        painAuxChocolat.put("size", "normal");

        newFavorite.add(painAuxChocolat);
        modifiedContent.set("favorites", newFavorite);

        JsonDocumentContent.build(source, modifiedContent);

        assertThat(source.at("/identity/age").intValue()).isEqualTo(32);
        assertThat(source.at("/identity/name").textValue()).isEqualTo("Julia");
        assertThat(source.at("/favorites/0/name").textValue()).isEqualTo("painAuxChocolat");
        assertThat(source.at("/favorites/0/size").textValue()).isEqualTo("normal");
    }

}