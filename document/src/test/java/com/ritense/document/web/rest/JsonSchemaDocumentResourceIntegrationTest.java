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

package com.ritense.document.web.rest;

import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.request.AssignToDocumentsRequest;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.web.rest.impl.JsonSchemaDocumentResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ritense.valtimo.contract.utils.TestUtil.convertObjectToJsonBytes;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class JsonSchemaDocumentResourceIntegrationTest extends BaseIntegrationTest {
    private static final String USER_EMAIL = "user@valtimo.nl";

    private Document document;
    private JsonSchemaDocumentResource jsonSchemaDocumentResource;
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void setUp() {
        var content = new JsonDocumentContent("{\"street\": \"Funenpark\"}");
        final JsonSchemaDocument.CreateDocumentResultImpl result = JsonSchemaDocument.create(
            definition(),
            content,
            USERNAME,
            documentSequenceGeneratorService,
            null
        );
        document = result.resultingDocument().orElseThrow();
        documentRepository.save(document);

        jsonSchemaDocumentResource = new JsonSchemaDocumentResource(documentService, documentDefinitionService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(jsonSchemaDocumentResource)
            .build();
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {FULL_ACCESS_ROLE})
    void shouldAssignUserToCase() throws Exception {
        var user = mockUser("John", "Doe");
        var loggedInUser = mockUser("Henk", "de Vries");
        when(userManagementService.findById(user.getId())).thenReturn(user);
        when(userManagementService.getCurrentUser()).thenReturn(loggedInUser);

        var postContent = "{ \"assigneeId\": \"" + user.getId() + "\"}";

        mockMvc.perform(
                post("/api/v1/document/{documentId}/assign", document.id().getId().toString())
                    .content(postContent)
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        // Assert that the assignee is saved in the document
        var result = documentRepository.findById(document.id());

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof JsonSchemaDocument);

        var savedDocument = (JsonSchemaDocument) result.get();
        assertNotNull(savedDocument.assigneeId());
        assertEquals(user.getId(), savedDocument.assigneeId());
        assertNotNull(savedDocument.assigneeFullName());
        assertEquals(user.getFullName(), savedDocument.assigneeFullName());
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {FULL_ACCESS_ROLE})
    void shouldAssignUserToMultipleCases() throws Exception {
        var content = new JsonDocumentContent("{\"street\": \"Park\"}");
        final JsonSchemaDocument.CreateDocumentResultImpl resultDoc = JsonSchemaDocument.create(
            definition(),
            content,
            USERNAME,
            documentSequenceGeneratorService,
            null
        );
        var document2 = resultDoc.resultingDocument().orElseThrow();
        documentRepository.save(document2);

        var user = mockUser("John", "Doe");
        var loggedInUser = mockUser("Henk", "de Vries");
        when(userManagementService.findById(user.getId())).thenReturn(user);
        when(userManagementService.getCurrentUser()).thenReturn(loggedInUser);

        AssignToDocumentsRequest assignToDocumentsRequest = new AssignToDocumentsRequest(List.of(document.id().getId(), document2.id().getId()), user.getId());

        mockMvc.perform(
                post("/api/v1/document/assign")
                    .characterEncoding(UTF_8)
                    .content(convertObjectToJsonBytes(assignToDocumentsRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        // Assert that the assignee is saved in the document
        var result1 = documentRepository.findById(document.id());
        var result2 = documentRepository.findById(document2.id());

        assertTrue(result1.isPresent());
        assertTrue(result1.get() instanceof JsonSchemaDocument);
        assertTrue(result2.isPresent());
        assertTrue(result2.get() instanceof JsonSchemaDocument);

        var savedDocument = (JsonSchemaDocument) result1.get();
        assertNotNull(savedDocument.assigneeId());
        assertEquals(user.getId(), savedDocument.assigneeId());
        assertNotNull(savedDocument.assigneeFullName());
        assertEquals(user.getFullName(), savedDocument.assigneeFullName());

        var savedDocument2 = (JsonSchemaDocument) result2.get();
        assertNotNull(savedDocument2.assigneeId());
        assertEquals(user.getId(), savedDocument2.assigneeId());
        assertNotNull(savedDocument2.assigneeFullName());
        assertEquals(user.getFullName(), savedDocument2.assigneeFullName());
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {FULL_ACCESS_ROLE})
    void shouldAssignUserToSingleCasesWithMultipleCasesPresent() throws Exception {
        var content = new JsonDocumentContent("{\"street\": \"Park\"}");
        final JsonSchemaDocument.CreateDocumentResultImpl resultDoc = JsonSchemaDocument.create(
            definition(),
            content,
            USERNAME,
            documentSequenceGeneratorService,
            null
        );
        var document2 = resultDoc.resultingDocument().orElseThrow();
        documentRepository.save(document2);

        var user = mockUser("John", "Doe");
        var loggedInUser = mockUser("Henk", "de Vries");
        when(userManagementService.findById(user.getId())).thenReturn(user);
        when(userManagementService.getCurrentUser()).thenReturn(loggedInUser);

        AssignToDocumentsRequest assignToDocumentsRequest = new AssignToDocumentsRequest(List.of(document.id().getId()), user.getId());

        mockMvc.perform(
                post("/api/v1/document/assign")
                    .characterEncoding(UTF_8)
                    .content(convertObjectToJsonBytes(assignToDocumentsRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        // Assert that the assignee is saved in the document
        var result1 = documentRepository.findById(document.id());
        var result2 = documentRepository.findById(document2.id());

        assertTrue(result1.isPresent());
        assertTrue(result1.get() instanceof JsonSchemaDocument);
        assertTrue(result2.isPresent());
        assertTrue(result2.get() instanceof JsonSchemaDocument);

        var savedDocument = (JsonSchemaDocument) result1.get();
        assertNotNull(savedDocument.assigneeId());
        assertEquals(user.getId(), savedDocument.assigneeId());
        assertNotNull(savedDocument.assigneeFullName());
        assertEquals(user.getFullName(), savedDocument.assigneeFullName());

        var savedDocument2 = (JsonSchemaDocument) result2.get();
        assertNull(savedDocument2.assigneeId());
        assertNull(savedDocument2.assigneeFullName());
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {FULL_ACCESS_ROLE})
    void shouldNotAssignInvalidUserId() throws Exception {
        var user = mockUser("John", "Doe");
        when(userManagementService.findById(user.getId())).thenReturn(null);

        var postContent = "{ \"assigneeId\": \"" + user.getId() + "\"}";

        mockMvc.perform(
                post("/api/v1/document/{documentId}/assign", document.id().getId().toString())
                    .content(user.getId())
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {FULL_ACCESS_ROLE})
    void shouldUnassignUserFromCase() throws Exception {
        var user = mockUser("John", "Doe");
        when(userManagementService.findById(user.getId())).thenReturn(user);

        mockMvc.perform(
                post("/api/v1/document/{documentId}/unassign", document.id().getId().toString())
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        // Assert that the assignee is saved in the document
        var result = documentRepository.findById(document.id());

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof JsonSchemaDocument);

        var savedDocument = (JsonSchemaDocument) result.get();
        assertNull(savedDocument.assigneeId());
        assertNull(savedDocument.assigneeFullName());
    }
}
