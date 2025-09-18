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

package com.ritense.document.repository.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ritense.BaseIntegrationTest;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Tag("integration")
class JsonSchemaDocumentRepositoryPessimisticLockTest extends BaseIntegrationTest {

    private static final String USERNAME = "test@test.com";

    @Autowired
    private JsonSchemaDocumentRepository documentRepository;

    private JsonSchemaDocument testDocument;
    private UUID documentId;

    @BeforeEach
    void setUp() {
        testDocument = createDocument("{}");
        documentId = testDocument.id().getId();
    }

    @AfterEach
    void tearDown() {
        documentRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {AuthoritiesConstants.ADMIN})
    @Transactional
    void shouldAcquirePessimisticLock() {
        Optional<JsonSchemaDocument> lockedDocument = documentRepository.findByIdForUpdate(documentId);

        assertTrue(lockedDocument.isPresent());
    }

    @Test
    @WithMockUser(username = USERNAME, authorities = {AuthoritiesConstants.ADMIN})
    @Transactional
    void shouldReturnEmptyForNonExistentDocument() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<JsonSchemaDocument> result = documentRepository.findByIdForUpdate(nonExistentId);

        assertTrue(result.isEmpty());
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