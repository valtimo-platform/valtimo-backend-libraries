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

package com.ritense.processdocument;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinitionId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstanceId;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public abstract class BaseTest {

    protected static final String USERNAME = "test@test.com";
    protected DocumentSequenceGeneratorService documentSequenceGeneratorService;

    protected static final String DOCUMENT_DEFINITION_NAME = "house";
    protected static final String PROCESS_INSTANCE_ID = UUID.randomUUID().toString();
    protected static final String PROCESS_DEFINITION_KEY = "def-key";

    public BaseTest() {
        documentSequenceGeneratorService = mock(DocumentSequenceGeneratorService.class);
        when(documentSequenceGeneratorService.next(any())).thenReturn(1L);
    }

    protected String readFileAsString(String fileName) throws IOException {
        return new String(getClass().getResource(fileName).openStream().readAllBytes());
    }

    protected CamundaProcessInstanceId processInstanceId() {
        return new CamundaProcessInstanceId(PROCESS_INSTANCE_ID);
    }

    protected JsonSchemaDocumentDefinitionId definitionId() {
        return JsonSchemaDocumentDefinitionId.newId(DOCUMENT_DEFINITION_NAME);
    }

    protected CamundaProcessDefinitionKey processDefinitionKey() {
        return new CamundaProcessDefinitionKey(PROCESS_DEFINITION_KEY);
    }

    protected CamundaProcessJsonSchemaDocumentDefinitionId processDocumentDefinitionId() {
        return CamundaProcessJsonSchemaDocumentDefinitionId.newId(processDefinitionKey(), definitionId());
    }

    protected JsonSchemaDocumentId documentId() {
        return JsonSchemaDocumentId.newId(UUID.randomUUID());
    }

    protected CamundaProcessJsonSchemaDocumentInstanceId processDocumentInstanceId() {
        return CamundaProcessJsonSchemaDocumentInstanceId.newId(processInstanceId(), documentId());
    }

    protected JsonSchemaDocumentDefinition definition() {
        final JsonSchemaDocumentDefinitionId jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("house");
        final JsonSchema jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()));
        return new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);
    }

    protected JsonSchemaDocumentDefinition definition(String name) {
        final JsonSchemaDocumentDefinitionId jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId(name);
        final JsonSchema jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()));
        return new JsonSchemaDocumentDefinition(jsonSchemaDocumentDefinitionId, jsonSchema);
    }

    protected JsonSchemaDocument.CreateDocumentResultImpl createDocument(JsonSchemaDocumentDefinition definition, JsonDocumentContent content) {
        return JsonSchemaDocument.create(definition, content, USERNAME, documentSequenceGeneratorService, null);
    }

    public URI path(String name) {
        return URI.create(String.format("config/document/definition/%s.json", name + ".schema"));
    }

}
