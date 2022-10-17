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
import com.ritense.document.event.DocumentAssigneeChangedEvent;
import com.ritense.document.exception.DocumentNotFoundException;
import com.ritense.document.exception.ModifyDocumentException;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.DocumentRepository;
import com.ritense.document.service.DocumentService;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.audit.utils.AuditHelper;
import com.ritense.valtimo.contract.authentication.NamedUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.authentication.model.SearchByUserGroupsCriteria;
import com.ritense.valtimo.contract.resource.Resource;
import com.ritense.valtimo.contract.utils.RequestHelper;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.Constants.SYSTEM_ACCOUNT;

public class JsonSchemaDocumentService implements DocumentService {

    private final DocumentRepository documentRepository;
    private final JsonSchemaDocumentDefinitionService documentDefinitionService;
    private final JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService;

    private final UserManagementService userManagementService;
    private final ResourceService resourceService;

    private final ApplicationEventPublisher applicationEventPublisher;

    public JsonSchemaDocumentService(DocumentRepository documentRepository,
                                     JsonSchemaDocumentDefinitionService documentDefinitionService,
                                     JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService,
                                     ResourceService resourceService,
                                     UserManagementService userManagementService,
                                     ApplicationEventPublisher applicationEventPublisher) {
        this.documentRepository = documentRepository;
        this.documentDefinitionService = documentDefinitionService;
        this.documentSequenceGeneratorService = documentSequenceGeneratorService;
        this.resourceService = resourceService;
        this.userManagementService = userManagementService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Optional<JsonSchemaDocument> findBy(Document.Id documentId) {
        return documentRepository.findById(documentId);
    }

    @Override
    public JsonSchemaDocument get(String documentId) {
        var documentOptional = findBy(
            JsonSchemaDocumentId.existingId(UUID.fromString(documentId))
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
        NewDocumentRequest newDocumentRequest
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
            JsonSchemaDocumentRelation.from(newDocumentRequest.documentRelation())
        );
        result.resultingDocument().ifPresent(jsonSchemaDocument -> {
            newDocumentRequest.getResources()
                    .stream()
                    .map(JsonSchemaRelatedFile::from)
                    .map(relatedFile -> relatedFile.withCreatedBy(SecurityUtils.getCurrentUserLogin()))
                    .forEach(jsonSchemaDocument::addRelatedFile);
            documentRepository.saveAndFlush(jsonSchemaDocument);
        });
        return result;
    }

    @Override
    @Transactional
    public void modifyDocument(Document document, JsonNode jsonNode) {
        final var documentRequest = ModifyDocumentRequest.create(document, jsonNode);
        final var modifyResult = modifyDocument(documentRequest);
        if (!modifyResult.errors().isEmpty()) {
            throw new ModifyDocumentException(modifyResult.errors());
        }
    }

    @Override
    @Transactional
    public synchronized JsonSchemaDocument.ModifyDocumentResultImpl modifyDocument(
        ModifyDocumentRequest request
    ) {
        final var documentId = JsonSchemaDocumentId.existingId(UUID.fromString(request.documentId()));
        final var version = JsonSchemaDocumentVersion.from(request.versionBasedOn());
        final var document = findBy(documentId)
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

        result.resultingDocument().ifPresent(documentRepository::saveAndFlush);
        return result;
    }

    @Override
    @Transactional
    public void assignDocumentRelation(
        Document.Id documentId,
        DocumentRelation documentRelation
    ) {
        final JsonSchemaDocumentRelation jsonSchemaDocumentRelation = JsonSchemaDocumentRelation.from(
            new DocumentRelationRequest(
                UUID.fromString(documentRelation.id().toString()),
                documentRelation.relationType()
            )
        );
        findBy(documentId)
            .ifPresent(document -> documentRepository.save(document.addRelatedDocument(jsonSchemaDocumentRelation)));
    }

    @Override
    @Transactional
    public void assignRelatedFile(
        final Document.Id documentId,
        final RelatedFile relatedFile
    ) {
        JsonSchemaDocument document = getDocumentBy(documentId);
        document.addRelatedFile(JsonSchemaRelatedFile.from(relatedFile));
        documentRepository.save(document);
    }

    @Override
    @Transactional
    public void assignResource(Document.Id documentId, UUID resourceId) {
        assignResource(documentId, resourceId, null);
    }

    @Override
    @Transactional
    public void assignResource(Document.Id documentId, UUID resourceId, Map<String, Object> metadata) {
        JsonSchemaDocument document = getDocumentBy(documentId);
        final Resource resource = resourceService.getResource(resourceId);
        document.addRelatedFile(JsonSchemaRelatedFile.from(resource).withCreatedBy(SecurityUtils.getCurrentUserLogin()), metadata);
        documentRepository.save(document);
    }

    @Override
    @Transactional
    public void removeRelatedFile(Document.Id documentId, UUID fileId) {
        JsonSchemaDocument document = getDocumentBy(documentId);
        document.removeRelatedFileBy(fileId);
        documentRepository.save(document);
    }

    public JsonSchemaDocument getDocumentBy(Document.Id documentId) {
        return findBy(documentId)
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
    public boolean currentUserCanAccessDocument(Document.Id documentId) {
        return findBy(documentId).map(document ->
            documentDefinitionService.currentUserCanAccessDocumentDefinition(document.definitionId().name())
        ).orElse(false);
    }

    @Override
    public void assignUserToDocument(UUID documentId, String assigneeId) {
        JsonSchemaDocument document = getDocumentBy(
            JsonSchemaDocumentId.existingId(documentId));

        var assignee = userManagementService.findById(assigneeId);
        if (assignee == null) {
            throw new IllegalArgumentException("Cannot set assignee for the invalid user id " + assigneeId);
        }

        document.setAssignee(assigneeId, assignee.getFullName());
        documentRepository.save(document);

        // Publish an event to update the audit log
        publishDocumentAssigneeChangedEvent(assignee.getFullName());
    }

    private void publishDocumentAssigneeChangedEvent(String assigneeFullName) {
        applicationEventPublisher.publishEvent(
            new DocumentAssigneeChangedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                assigneeFullName
            )
        );
    }

    @Override
    public Set<String> getDocumentRoles(Document.Id documentId) {
        var document = get(documentId.toString());
        return documentDefinitionService.getDocumentDefinitionRoles(document.definitionId().name());
    }

    @Override
    public List<NamedUser> getCandidateUsers(Document.Id documentId) {
        var searchCriteria = new SearchByUserGroupsCriteria();
        searchCriteria.addToOrUserGroups(getDocumentRoles(documentId));
        return userManagementService.findByRoles(searchCriteria).stream()
            .map(user -> NamedUser.from(user))
            .collect(Collectors.toList());
    }
}
