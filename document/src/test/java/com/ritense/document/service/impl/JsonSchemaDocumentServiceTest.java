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

package com.ritense.document.service.impl;

import com.ritense.document.BaseTest;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.event.DocumentUnassignedEvent;
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository;
import com.ritense.authorization.AuthorizationService;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonSchemaDocumentServiceTest extends BaseTest {

    private JsonSchemaDocumentService jsonSchemaDocumentService;
    private JsonSchemaDocumentRepository documentRepository;
    private JsonSchemaDocumentDefinitionService documentDefinitionService;
    private JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService;
    private ResourceService resourceService;

    private AuthorizationService authorizationService;

    private UserManagementService userManagementService;
    private ApplicationEventPublisher applicationEventPublisher;
    private JsonSchemaDocument jsonSchemaDocument;

    private final String documentDefinitionName = "name";

    @BeforeEach
    void init() {
        documentRepository = mock(JsonSchemaDocumentRepository.class);
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        documentSequenceGeneratorService = mock(JsonSchemaDocumentDefinitionSequenceGeneratorService.class);
        resourceService = mock(ResourceService.class);
        authorizationService = mock(AuthorizationService.class);
        userManagementService = mock(UserManagementService.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        jsonSchemaDocumentService = new JsonSchemaDocumentService(
            documentRepository,
            documentDefinitionService,
            documentSequenceGeneratorService,
            resourceService,
            userManagementService,
            authorizationService,
            applicationEventPublisher);

        var content = new JsonDocumentContent("{\"firstname\": \"aName\"}");
        jsonSchemaDocument = createDocument(definitionOf("person"), content).resultingDocument().orElseThrow();
    }

    @Test
    void shouldCreateDocument() {
        final var content = new JsonDocumentContent("{\"addresses\" : [{\"streetName\" : \"Funenpark\"}]}");
        NewDocumentRequest documentRequest = new NewDocumentRequest(
            "document-definition",
            content.asJson()
        );

        JsonSchemaDocumentDefinition definition = definitionOf("referenced-array");
        when(documentDefinitionService.findLatestByName(eq("document-definition"))).thenReturn(Optional.of(definition));
        when(documentSequenceGeneratorService.next(definition.id())).thenReturn(123L);

        CreateDocumentResult result = jsonSchemaDocumentService.createDocument(documentRequest);
        JsonSchemaDocument document = (JsonSchemaDocument)result.resultingDocument().get();

        assertEquals(content.asJson(), document.content().asJson());
        assertEquals(definition.id(), document.definitionId());
        assertEquals(123L, document.sequence());
        assertEquals("system", document.createdBy());
        assertNotNull(document.createdOn());
        verify(documentRepository, times(1)).saveAndFlush(document);
    }

    @Test
    void shouldCreateDocumentWithResources() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("user@ritense.com", USER));
        final var content = new JsonDocumentContent("{\"addresses\" : [{\"streetName\" : \"Funenpark\"}]}");

        LocalDateTime createdOn = LocalDateTime.now();
        UUID resourceId = UUID.randomUUID();
        Resource resource = new Resource() {
            @Override public UUID id() {return resourceId;}
            @Override public String name() {return "name.txt";}
            @Override public String extension() {return "txt";}
            @Override public Long sizeInBytes() {return 123L;}
            @Override public LocalDateTime createdOn() {return createdOn;}
        };

        NewDocumentRequest documentRequest = new NewDocumentRequest(
            "document-definition",
            content.asJson()
        );
        documentRequest.withResources(Set.of(resource));

        JsonSchemaDocumentDefinition definition = definitionOf("referenced-array");
        when(documentDefinitionService.findLatestByName(eq("document-definition"))).thenReturn(Optional.of(definition));
        when(documentSequenceGeneratorService.next(definition.id())).thenReturn(123L);

        CreateDocumentResult result = jsonSchemaDocumentService.createDocument(documentRequest);
        JsonSchemaDocument document = (JsonSchemaDocument)result.resultingDocument().get();

        assertEquals(content.asJson(), document.content().asJson());
        assertEquals(definition.id(), document.definitionId());
        assertEquals(123L, document.sequence());
        assertEquals("user@ritense.com", document.createdBy());
        assertNotNull(document.createdOn());
        assertEquals(document.relatedFiles().size(), 1);
        document.relatedFiles().forEach(relatedFile -> {
            assertEquals(resourceId, relatedFile.getFileId());
            assertEquals("name.txt", relatedFile.getFileName());
            assertEquals(createdOn, relatedFile.getCreatedOn());
            assertEquals("user@ritense.com", relatedFile.getCreatedBy());
            assertEquals(123L, relatedFile.getSizeInBytes());
        });

        verify(documentRepository, times(1)).saveAndFlush(document);
    }

    @Test
    void shouldRemoveDocuments() {
        PageImpl<JsonSchemaDocument> jsonSchemaDocuments = new PageImpl<>(List.of(this.jsonSchemaDocument));

        when(documentRepository.findAllByDocumentDefinitionIdName(eq(Pageable.unpaged()), eq(documentDefinitionName)))
            .thenReturn(jsonSchemaDocuments);

        jsonSchemaDocumentService.removeDocuments(documentDefinitionName);

        verify(documentRepository, times(1)).saveAll(eq(jsonSchemaDocuments.toList()));
        verify(documentRepository, times(1)).deleteAll(eq(jsonSchemaDocuments.toList()));
        verify(documentSequenceGeneratorService, times(1)).deleteSequenceRecordBy(eq(documentDefinitionName));
    }

    @Test
    void shouldNotRemoveDocumentsBecauseTheyDontExist() {
        PageImpl<JsonSchemaDocument> jsonSchemaDocuments = new PageImpl<>(Collections.emptyList());

        when(documentRepository.findAllByDocumentDefinitionIdName(eq(Pageable.unpaged()), eq(documentDefinitionName)))
            .thenReturn(jsonSchemaDocuments);

        jsonSchemaDocumentService.removeDocuments(documentDefinitionName);

        verify(documentRepository, times(0)).saveAll(eq(jsonSchemaDocuments.toList()));
        verify(documentRepository, times(0)).deleteAll(eq(jsonSchemaDocuments.toList()));
        verify(documentSequenceGeneratorService, times(0)).deleteSequenceRecordBy(eq(documentDefinitionName));
    }

    @Test
    void shouldUnassignUserFromDocument() {
        jsonSchemaDocument.setAssignee("my-id", "John Doe");
        when(documentRepository.findById(jsonSchemaDocument.id())).thenReturn(Optional.of(jsonSchemaDocument));

        jsonSchemaDocumentService.unassignUserFromDocument(jsonSchemaDocument.id().getId());

        assertEquals(null, jsonSchemaDocument.assigneeId());
        assertEquals(null, jsonSchemaDocument.assigneeFullName());
        var captor = ArgumentCaptor.forClass(DocumentUnassignedEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        assertEquals("Anonymous", captor.getValue().getUser());
    }
}
