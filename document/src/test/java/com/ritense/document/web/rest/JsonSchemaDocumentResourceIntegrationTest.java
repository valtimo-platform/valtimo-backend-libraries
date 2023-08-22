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
import com.ritense.document.WithMockTenantUser;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.web.rest.impl.JsonSchemaDocumentResource;
import com.ritense.tenancy.TenantResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
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
    private JsonSchemaDocument document;
    private JsonSchemaDocumentResource jsonSchemaDocumentResource;
    private MockMvc mockMvc;

    @Autowired
    private TenantResolver tenantResolver;

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
            null,
            TENANT_ID
        );
        document = result.resultingDocument().orElseThrow();
        documentRepository.saveAndFlush(document);

        documentDefinitionService.putDocumentDefinitionRoles(
            document.definitionId().name(),
            Set.of(USER)
        );

        jsonSchemaDocumentResource = new JsonSchemaDocumentResource(
            documentService,
            documentDefinitionService,
            tenantResolver
        );
        mockMvc = MockMvcBuilders
            .standaloneSetup(jsonSchemaDocumentResource)
            .build();
    }

    @Test
    @WithMockTenantUser
    void shouldAssignUserToCase() throws Exception {
        var user = mockUser("John", "Doe");
        when(userManagementService.findById(user.getId())).thenReturn(user);

        var postContent = "{ \"assigneeId\": \"" + user.getId() + "\"}";

        mockMvc.perform(
                post("/api/v1/document/{documentId}/assign", document.id().getId().toString())
                    .content(postContent)
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        // Assert that the assignee is saved in the document
        var result = documentRepository.findByIdAndTenantId(document.id(), TENANT_ID);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof JsonSchemaDocument);

        var savedDocument = (JsonSchemaDocument) result.get();
        assertNotNull(savedDocument.assigneeId());
        assertEquals(user.getId(), savedDocument.assigneeId());
        assertNotNull(savedDocument.assigneeFullName());
        assertEquals(user.getFullName(), savedDocument.assigneeFullName());
    }

    @Test
    @WithMockTenantUser
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
    @WithMockTenantUser
    void shouldUnassignUserFromCase() throws Exception {
        var user = mockUser("John", "Doe");
        when(userManagementService.findById(user.getId())).thenReturn(user);

        mockMvc.perform(
                post("/api/v1/document/{documentId}/unassign", document.id().getId().toString())
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        // Assert that the assignee is saved in the document
        var result = documentRepository.findByIdAndTenantId(document.id(), TENANT_ID);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof JsonSchemaDocument);

        var savedDocument = (JsonSchemaDocument) result.get();
        assertNull(savedDocument.assigneeId());
        assertNull(savedDocument.assigneeFullName());
    }

}
