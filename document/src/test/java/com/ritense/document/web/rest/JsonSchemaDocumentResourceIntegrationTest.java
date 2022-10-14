package com.ritense.document.web.rest;

import com.ritense.document.BaseIntegrationTest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.web.rest.impl.JsonSchemaDocumentResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
class JsonSchemaDocumentResourceIntegrationTest extends BaseIntegrationTest {
    private static final String USER_EMAIL = "user@valtimo.nl";

    private Document document;
    private JsonSchemaDocumentResource jsonSchemaDocumentResource;
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void setUp() {
        var content = new JsonDocumentContent("{\"firstName\": \"John\"}");
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
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void shouldAssignUserToCase() throws Exception {
        var user = mockUser("John", "Doe");
        when(userManagementService.findById(user.getId())).thenReturn(user);

        mockMvc.perform(
            post("/api/document/{documentId}/assign", document.id().getId().toString())
                .content(user.getId()))
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
    @WithMockUser(username = USER_EMAIL, authorities = {USER})
    void shouldNotAssignInvalidUserId() throws Exception {
        var user = mockUser("John", "Doe");
        when(userManagementService.findById(user.getId())).thenReturn(null);

        mockMvc.perform(
                post("/api/document/{documentId}/assign", document.id().getId().toString())
                    .content(user.getId()))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}
