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

package com.ritense.document;

import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaRelatedFile;
import com.ritense.document.service.DocumentSequenceGeneratorService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseTest {

    protected static final String USERNAME = "test@test.com";
    protected DocumentSequenceGeneratorService documentSequenceGeneratorService;

    public BaseTest() {
        documentSequenceGeneratorService = mock(DocumentSequenceGeneratorService.class);
        when(documentSequenceGeneratorService.next(any())).thenReturn(1L);
    }

    protected JsonSchemaDocumentDefinition definition() {
        final var jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("house");
        final var jsonSchema = JsonSchema.fromResource(jsonSchemaDocumentDefinitionId.path());
        return new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);
    }

    protected JsonSchemaDocumentDefinition definitionOf(String name) {
        final var documentDefinitionName = JsonSchemaDocumentDefinitionId.newId(name);
        final var schema = JsonSchema.fromResource(documentDefinitionName.path());
        return new JsonSchemaDocumentDefinition(documentDefinitionName, schema);
    }

    protected JsonSchemaDocumentDefinition definitionOf(String name, long version, String schemaPath) {
        final var documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId(name, version);
        final var schema = JsonSchema.fromResource(URI.create("config/document/definition/" + schemaPath));
        return new JsonSchemaDocumentDefinition(documentDefinitionId, schema);
    }

    protected JsonSchemaDocument createDocument(JsonDocumentContent content) {
        return JsonSchemaDocument
            .create(definition(), content, USERNAME, documentSequenceGeneratorService, null)
            .resultingDocument()
            .orElseThrow();
    }

    protected JsonSchemaDocument.CreateDocumentResultImpl createDocument(JsonSchemaDocumentDefinition definition, JsonDocumentContent content) {
        return JsonSchemaDocument.create(definition, content, USERNAME, documentSequenceGeneratorService, null);
    }

    protected JsonSchemaRelatedFile relatedFile() {
        return new JsonSchemaRelatedFile(
            UUID.randomUUID(),
            "Some-Name",
            1L,
            LocalDateTime.now(),
            "some-body"
        );
    }

}
