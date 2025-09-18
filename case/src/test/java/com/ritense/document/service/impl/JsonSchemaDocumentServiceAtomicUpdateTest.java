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

package com.ritense.document.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.BaseIntegrationTest;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.exception.DocumentNotFoundException;
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

@Tag("integration")
class JsonSchemaDocumentServiceAtomicUpdateTest extends BaseIntegrationTest {

    private static final String USERNAME = "test@test.com";

    @Autowired
    private JsonSchemaDocumentService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonSchemaDocument testDocument;
    private UUID documentId;

    @BeforeEach
    void setUp() {
        testDocument = createDocument("""
            {
                "person": {
                    "firstName": "John",
                    "lastName": "Doe"
                },
                "counter": 0
            }
            """);
        documentId = testDocument.id().getId();
    }

    @AfterEach
    void tearDown() {
        documentRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {AuthoritiesConstants.ADMIN})
    void shouldUpdateDocumentAtomically() {
        Document updatedDocument = documentService.updateDocumentAtomic(documentId, document -> {
            JsonSchemaDocument jsonDoc = (JsonSchemaDocument) document;
            try {
                var contentNode = objectMapper.readTree(jsonDoc.content().asJson().toString());
                ((com.fasterxml.jackson.databind.node.ObjectNode) contentNode).put("person.firstName", "Jane");
                var newContent = JsonDocumentContent.build(
                    jsonDoc.content().asJson(),
                    contentNode,
                    null
                );
                return jsonDoc.applyModifiedContent(newContent, jsonDoc.documentDefinition()).resultingDocument().orElseThrow();
            } catch (Exception e) {
                throw new RuntimeException("Failed to modify document", e);
            }
        });

        assertNotNull(updatedDocument);
        assertEquals(documentId, updatedDocument.id().getId());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {AuthoritiesConstants.ADMIN})
    void shouldIncrementVersionAtomically() {
        Integer originalVersion = testDocument.version();

        documentService.updateDocumentAtomic(documentId, document -> {
            JsonSchemaDocument jsonDoc = (JsonSchemaDocument) document;
            try {
                var contentNode = objectMapper.readTree(jsonDoc.content().asJson().toString());
                int currentCounter = contentNode.get("counter").asInt();
                ((com.fasterxml.jackson.databind.node.ObjectNode) contentNode).put("counter", currentCounter + 1);

                var newContent = JsonDocumentContent.build(
                    jsonDoc.content().asJson(),
                    contentNode,
                    null
                );
                return jsonDoc.applyModifiedContent(newContent, jsonDoc.documentDefinition()).resultingDocument().orElseThrow();
            } catch (Exception e) {
                throw new RuntimeException("Failed to modify document", e);
            }
        });

        JsonSchemaDocument updatedDoc = (JsonSchemaDocument) documentService.get(documentId.toString());
        assertEquals(originalVersion + 1, updatedDoc.version());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {AuthoritiesConstants.ADMIN})
    void shouldThrowExceptionForNonExistentDocument() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(DocumentNotFoundException.class, () ->
            documentService.updateDocumentAtomic(nonExistentId, document -> document)
        );
    }

    private JsonSchemaDocument createDocument(String content) {
        return AuthorizationContext.runWithoutAuthorization(() -> {
            var createRequest = new NewDocumentRequest(
                "allows-all",
                "allows-all",
                "1.0.0",
                new JsonDocumentContent(content).asJson()
            );

            var result = documentService.createDocument(createRequest);
            return (JsonSchemaDocument) result.resultingDocument().orElseThrow();
        });
    }
}