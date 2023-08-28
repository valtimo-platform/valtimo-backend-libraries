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

package com.ritense.document.domain.impl;

import com.ritense.document.TestHelper;
import org.everit.json.schema.ValidationException;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonSchemaDocumentDefinitionTest extends TestHelper {

    @Test
    public void shouldNotCreateDocumentDefinitionWithInvalidJsonSchema() {
        final JsonSchemaDocumentDefinitionId jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("invalidperson");
        assertThrows(ValidationException.class, () -> JsonSchema.fromResourceUri(URI.create("config/data/invalidperson.schema.json")));
    }

    @Test
    public void shouldCreateDocumentDefinition() {
        final JsonSchemaDocumentDefinitionId jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("person");
        final JsonSchema jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()));
        final var jsonSchemaDocumentDefinition = new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);

        assertThat(jsonSchemaDocumentDefinition.id()).isEqualTo(jsonSchemaDocumentDefinitionId);
        assertThat(jsonSchemaDocumentDefinition.schema().toString()).isEqualTo(jsonSchema.asJson().toString());
        assertThat(jsonSchemaDocumentDefinition.isReadOnly()).isFalse();
    }

    @Test
    public void shouldMarkReadOnly() {
        final JsonSchemaDocumentDefinitionId jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("person");
        final JsonSchema jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()));
        final var jsonSchemaDocumentDefinition = new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);

        jsonSchemaDocumentDefinition.markReadOnly();

        assertThat(jsonSchemaDocumentDefinition.isReadOnly()).isTrue();
    }

}
