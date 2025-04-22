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

package com.ritense.document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaRelatedFile;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseTest {

    protected static final String USERNAME = "test@test.com";
    protected DocumentSequenceGeneratorService documentSequenceGeneratorService;
    protected TestHelper testHelper;

    public BaseTest() {
        testHelper = new TestHelper();
        documentSequenceGeneratorService = mock(DocumentSequenceGeneratorService.class);
        when(documentSequenceGeneratorService.next(any())).thenReturn(1L);
    }

    protected JsonSchemaDocumentDefinition definition() {
        final var jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.of("house", caseDefinitionId());
        final var schema = JsonSchema.fromResourceUri(path(
            jsonSchemaDocumentDefinitionId.caseDefinitionId(),
            jsonSchemaDocumentDefinitionId.name()
        ));
        return new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, schema);
    }

    protected JsonSchemaDocumentDefinition definitionOf(String name) {
        final var documentDefinitionName = JsonSchemaDocumentDefinitionId.of(name, caseDefinitionId());
        final var schema = JsonSchema.fromResourceUri(path(
            documentDefinitionName.caseDefinitionId(),
            documentDefinitionName.name()
        ));
        return new JsonSchemaDocumentDefinition(documentDefinitionName, schema);
    }

    protected JsonSchemaDocumentDefinition definitionOf(JsonSchemaDocumentDefinitionId documentDefinitionId) {
        final var schema = JsonSchema.fromResourceUri(path(
            documentDefinitionId.caseDefinitionId(),
            documentDefinitionId.name()
        ));
        return new JsonSchemaDocumentDefinition(documentDefinitionId, schema);
    }

    protected JsonSchemaDocumentDefinition definitionOfForUnitTests(String name) {
        final var documentDefinitionName = JsonSchemaDocumentDefinitionId.of(name, caseDefinitionId());
        final var schema = JsonSchema.fromResourceUri(testHelper.path(
            documentDefinitionName.name()
        ));
        return new JsonSchemaDocumentDefinition(documentDefinitionName, schema);
    }

    protected CaseDefinitionId caseDefinitionId() {
        return CaseDefinitionId.of("house", "1.1.0");
    }

    protected JsonSchemaDocument createDocument() {
        final var json = "{\"firstName\": \"John\"}";
        final var content = new JsonDocumentContent(json);
        return createDocument(content);
    }

    protected JsonSchemaDocument createDocument(JsonDocumentContent content) {
        return JsonSchemaDocument
            .create(definition(), content, USERNAME, documentSequenceGeneratorService, null)
            .resultingDocument()
            .orElseThrow();
    }

    protected JsonSchemaDocument.CreateDocumentResultImpl createDocument(
        JsonSchemaDocumentDefinition definition,
        JsonDocumentContent content
    ) {
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

    public URI path(CaseDefinitionId caseDefinitionId, String name) {
        var caseDefinitionVersion = caseDefinitionId.getVersionTag();
        var formattedCaseDefinitionVersion = caseDefinitionVersion.getMajor() +
            "-" + caseDefinitionVersion.getMinor() +
            "-" + caseDefinitionVersion.getPatch();
        return URI.create(String.format(
            "config/case/%s/%s/document/definition/%s.json",
            caseDefinitionId.getKey(),
            formattedCaseDefinitionVersion,
            name + ".schema"
        ));
    }

}
