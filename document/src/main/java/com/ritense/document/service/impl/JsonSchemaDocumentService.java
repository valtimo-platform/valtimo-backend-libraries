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

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.RelatedFile;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.JsonSchemaDocumentVersion;
import com.ritense.document.domain.impl.JsonSchemaRelatedFile;
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation;
import com.ritense.document.domain.impl.request.DocumentRelationRequest;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.relation.DocumentRelation;
import com.ritense.document.exception.DocumentNotFoundException;
import com.ritense.document.exception.ModifyDocumentException;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.service.DocumentService;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.authentication.NamedUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.ritense.valtimo.contract.Constants.SYSTEM_ACCOUNT;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class JsonSchemaDocumentService implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDocumentService.class);

    private final DocumentRepository documentRepository;
    private final JsonSchemaDocumentDefinitionService documentDefinitionService;
    private final JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService;
    private final UserManagementService userManagementService;
    private final ResourceService resourceService;

    public JsonSchemaDocumentService(
        final DocumentRepository documentRepository,
        final JsonSchemaDocumentDefinitionService documentDefinitionService,
        final JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService,
        final ResourceService resourceService,
        final UserManagementService userManagementService
    ) {
        this.documentRepository = documentRepository;
        this.documentDefinitionService = documentDefinitionService;
        this.documentSequenceGeneratorService = documentSequenceGeneratorService;
        this.resourceService = resourceService;
        this.userManagementService = userManagementService;
    }

    @Override
    public Optional<JsonSchemaDocument> findBy(Document.Id documentId, String tenantId) {
        assertArgumentNotNull(documentId, "documentId is required");
        assertArgumentNotEmpty(tenantId, "tenantId is required");
        return documentRepository.findByIdAndTenantId(documentId, tenantId);
    }

    @Override
    public JsonSchemaDocument get(String documentId, String tenantId) {
        var documentOptional = findBy(
            JsonSchemaDocumentId.existingId(UUID.fromString(documentId)),
            tenantId
        );
        return documentOptional.orElseThrow(
            () -> new DocumentNotFoundException("Document not found with id " + documentId)
        );
    }

    @Override
    public Page<JsonSchemaDocument> getAllByDocumentDefinitionName(Pageable pageable, String definitionName) {
        return documentRepository.findAllByDocumentDefinitionIdName(pageable, definitionName);
    }

    @Override
    public Page<JsonSchemaDocument> getAll(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public JsonSchemaDocument.CreateDocumentResultImpl createDocument(
        final NewDocumentRequest newDocumentRequest
    ) {
        final JsonSchemaDocumentDefinition definition = documentDefinitionService
            .findLatestByName(newDocumentRequest.documentDefinitionName())
            .orElseThrow(() -> new UnknownDocumentDefinitionException(newDocumentRequest.documentDefinitionName()));
        final var content = JsonDocumentContent.build(newDocumentRequest.content());
        final var user = SecurityUtils.getCurrentUserLogin() != null ? SecurityUtils.getCurrentUserLogin() : SYSTEM_ACCOUNT;

        final var result = JsonSchemaDocument.create(
            definition,
            content,
            user,
            documentSequenceGeneratorService,
            JsonSchemaDocumentRelation.from(newDocumentRequest.documentRelation()),
            newDocumentRequest.tenantId()
        );
        result.resultingDocument().ifPresent(
            document -> {
                newDocumentRequest.getResources()
                    .stream()
                    .map(JsonSchemaRelatedFile::from)
                    .map(relatedFile -> relatedFile.withCreatedBy(SecurityUtils.getCurrentUserLogin()))
                    .forEach(document::addRelatedFile);
                documentRepository.insert(document);
            }
        );
        return result;
    }

    @Override
    @Transactional
    public void modifyDocument(
        final Document document,
        final JsonNode jsonNode,
        final String tenantId
    ) {
        final var documentRequest = ModifyDocumentRequest.create(document, jsonNode, tenantId);
        final var modifyResult = modifyDocument(documentRequest);
        if (!modifyResult.errors().isEmpty()) {
            throw new ModifyDocumentException(modifyResult.errors());
        }
    }

    @Override
    @Transactional(timeout = 30, rollbackFor = {Exception.class})
    public synchronized JsonSchemaDocument.ModifyDocumentResultImpl modifyDocument(
        final ModifyDocumentRequest request
    ) {
        final var documentId = JsonSchemaDocumentId.existingId(UUID.fromString(request.documentId()));
        final var version = JsonSchemaDocumentVersion.from(request.versionBasedOn());
        final var document = findBy(documentId, request.tenantId())
            .orElseThrow(() -> new DocumentNotFoundException("Document not found with id " + request.documentId()));

        final var modifiedContent = JsonDocumentContent.build(
            document.content().asJson(),
            request.content(),
            request.jsonPatch()
        );
        var documentDefinition = documentDefinitionService.findBy(document.definitionId()).orElseThrow();
        final var result = document.applyModifiedContent(
            modifiedContent,
            documentDefinition,
            version
        );
        result.resultingDocument().ifPresent(documentRepository::update);
        return result;
    }

    @Override
    @Transactional
    public void assignDocumentRelation(
        Document.Id documentId,
        DocumentRelation documentRelation,
        String tenantId
    ) {
        final JsonSchemaDocumentRelation jsonSchemaDocumentRelation = JsonSchemaDocumentRelation.from(
            new DocumentRelationRequest(
                UUID.fromString(documentRelation.id().toString()),
                documentRelation.relationType()
            )
        );
        findBy(documentId, tenantId)
            .ifPresent(
                document -> {
                    document.addRelatedDocument(jsonSchemaDocumentRelation);
                    documentRepository.update(document);
                }
            );
    }

    @Override
    @Transactional
    public void assignRelatedFile(
        final Document.Id documentId,
        final RelatedFile relatedFile,
        final String tenantId
    ) {
        final var document = getDocumentBy(documentId, tenantId);
        document.addRelatedFile(JsonSchemaRelatedFile.from(relatedFile));
        documentRepository.update(document);
    }

    @Override
    @Transactional
    public void assignResource(Document.Id documentId, UUID resourceId, String tenantId) {
        assignResource(documentId, resourceId, null, tenantId);
    }

    @Override
    @Transactional
    public void assignResource(
        final Document.Id documentId,
        final UUID resourceId,
        final Map<String, Object> metadata,
        final String tenantId
    ) {
        final var document = getDocumentBy(documentId, tenantId);
        final var resource = resourceService.getResource(resourceId);
        document.addRelatedFile(
            JsonSchemaRelatedFile.from(resource)
                .withCreatedBy(SecurityUtils.getCurrentUserLogin()),
            metadata
        );
        documentRepository.update(document);
    }

    @Override
    @Transactional
    public void removeRelatedFile(Document.Id documentId, UUID fileId, String tenantId) {
        final JsonSchemaDocument document = getDocumentBy(documentId, tenantId);
        document.removeRelatedFileBy(fileId);
        documentRepository.update(document);
    }

    public JsonSchemaDocument getDocumentBy(Document.Id documentId, String tenantId) {
        assertArgumentNotEmpty(tenantId, "tenantId is required");
        return findBy(documentId, tenantId)
            .orElseThrow(() -> new DocumentNotFoundException("Unable to find document with ID " + documentId));
    }

    @Override
    public void removeDocuments(String documentDefinitionName) {
        List<JsonSchemaDocument> documents = getAllByDocumentDefinitionName(Pageable.unpaged(), documentDefinitionName).toList();
        if (!documents.isEmpty()) {
            documents.forEach(JsonSchemaDocument::removeAllRelatedFiles);
            documentRepository.saveAll(documents);
            documentRepository.deleteAll(documents);
            documentSequenceGeneratorService.deleteSequenceRecordBy(documentDefinitionName);
        }
    }

    @Override
    public boolean currentUserCanAccessDocument(Document.Id documentId, String tenantId) {
        return findBy(documentId, tenantId)
            .map(document -> documentDefinitionService.currentUserCanAccessDocumentDefinition(
                    document.definitionId().name()
                )
            ).orElse(false);
    }

    @Override
    public void assignUserToDocument(UUID documentId, String assigneeId, String tenantId) {
        final var document = getDocumentBy(JsonSchemaDocumentId.existingId(documentId), tenantId);
        var assignee = userManagementService.findById(assigneeId);
        if (assignee == null) {
            logger.debug("Cannot set assignee for the invalid user id {}", assigneeId);
            throw new IllegalArgumentException("Cannot set assignee for the invalid user id " + assigneeId);
        }
        document.setAssignee(assigneeId, assignee.getFullName());
        documentRepository.update(document);
    }

    @Override
    public void unassignUserFromDocument(UUID documentId, String tenantId) {
        final var document = getDocumentBy(JsonSchemaDocumentId.existingId(documentId), tenantId);
        document.unassign();
        documentRepository.update(document);
    }

    @Override
    public Set<String> getDocumentRoles(Document.Id documentId, String tenantId) {
        var document = get(documentId.toString(), tenantId);
        return documentDefinitionService.getDocumentDefinitionRoles(document.definitionId().name());
    }

    @Override
    public List<NamedUser> getCandidateUsers(Document.Id documentId, String tenantId) {
        return userManagementService.findNamedUserByRoles(getDocumentRoles(documentId, tenantId));
    }

}