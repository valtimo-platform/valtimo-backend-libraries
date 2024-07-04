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

package com.ritense.document.domain.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.valtimo.contract.json.patch.JsonPatch;
import com.ritense.valtimo.contract.json.patch.operation.JsonPatchOperation;
import com.ritense.valtimo.contract.json.patch.operation.RemoveOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashSet;

class JsonDocumentContentTest {

    private ObjectNode source;

    private static final String AGE_KEY = "age";
    private static final String FAVOURITES_KEY = "favourites";
    private static final String IDENTITY_KEY = "identity";
    private static final String NAME_KEY = "name";
    private static final String SIZE_KEY = "size";

    private static final String AGE_POINTER = "/identity/age";
    private static final String NAME_POINTER = "/identity/name";

    @BeforeEach
    void setUp() {
        source = JsonNodeFactory.instance.objectNode();

        ArrayNode favorites = JsonNodeFactory.instance.arrayNode();
        ObjectNode identity = JsonNodeFactory.instance.objectNode();
        identity.put(AGE_KEY, 32);
        identity.put(NAME_KEY, "Julia");

        ObjectNode bread = JsonNodeFactory.instance.objectNode();
        bread.put(NAME_KEY, "bread");
        bread.put(SIZE_KEY, "small");

        ObjectNode croissaint = JsonNodeFactory.instance.objectNode();
        croissaint.put(NAME_KEY, "croissaint");
        croissaint.put(SIZE_KEY, "large");

        favorites.add(bread);
        favorites.add(croissaint);

        source.set(IDENTITY_KEY, identity);
        source.set(FAVOURITES_KEY, favorites);
    }

    @Test
    void shouldRemoveArrayItemOnlyWithBuild() {
        ObjectNode modifiedContent = JsonNodeFactory.instance.objectNode();
        ArrayNode newFavorite = JsonNodeFactory.instance.arrayNode();

        ObjectNode painAuxChocolat = JsonNodeFactory.instance.objectNode();
        painAuxChocolat.put(NAME_KEY, "painAuxChocolat");
        painAuxChocolat.put(SIZE_KEY, "normal");

        newFavorite.add(painAuxChocolat);
        modifiedContent.set(FAVOURITES_KEY, newFavorite);

        JsonDocumentContent.build(source, modifiedContent);

        assertThat(source.at(AGE_POINTER).intValue()).isEqualTo(32);
        assertThat(source.at(NAME_POINTER).textValue()).isEqualTo("Julia");
        assertThat(source.at("/" + FAVOURITES_KEY + "/0/" + NAME_KEY).textValue()).isEqualTo("painAuxChocolat");
        assertThat(source.at("/" + FAVOURITES_KEY + "/0/" + SIZE_KEY).textValue()).isEqualTo("normal");
    }

    @Test
    void shouldRemoveItemViaPrePatchOnlyWithBuild() {
        // try to remove name from identity the normal way, it should fail
        ObjectNode modifiedIdentityContent = JsonNodeFactory.instance.objectNode();

        ObjectNode newIdentity = JsonNodeFactory.instance.objectNode();
        newIdentity.put(AGE_KEY, "32");

        modifiedIdentityContent.set(IDENTITY_KEY, newIdentity);

        JsonDocumentContent.build(source, modifiedIdentityContent);

        assertThat(source.at(NAME_POINTER).isMissingNode()).isFalse();

        // try to remove the name from the identity using pre patch, it should succeed
        ObjectNode emptyModifiedContent = JsonNodeFactory.instance.objectNode();

        LinkedHashSet<JsonPatchOperation> patches = new LinkedHashSet<>();
        patches.add(new RemoveOperation(JsonPointer.valueOf(NAME_POINTER)));
        JsonPatch prePatch = new JsonPatch(patches);
        JsonDocumentContent.build(source, emptyModifiedContent, prePatch);

        assertThat(source.at(NAME_POINTER).isMissingNode()).isTrue();
    }

}