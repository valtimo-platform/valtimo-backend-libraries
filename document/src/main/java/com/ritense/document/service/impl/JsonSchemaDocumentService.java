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
import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationRequest;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.AuthorizationSpecification;
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
import com.ritense.document.event.DocumentUnassignedEvent;
import com.ritense.document.exception.DocumentNotFoundException;
import com.ritense.document.exception.ModifyDocumentException;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.JsonSchemaDocumentSpecification;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.audit.utils.AuditHelper;
import com.ritense.valtimo.contract.authentication.NamedUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.resource.Resource;
import com.ritense.valtimo.contract.utils.RequestHelper;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.contract.Constants.SYSTEM_ACCOUNT;

public class JsonSchemaDocumentService implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDocumentService.class);

    private final JsonSchemaDocumentRepository documentRepository;
    private final JsonSchemaDocumentDefinitionService documentDefinitionService;
    private final JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService;

    private final UserManagementService userManagementService;
    private final ResourceService resourceService;

    private final AuthorizationService authorizationService;

    private final ApplicationEventPublisher applicationEventPublisher;

    public JsonSchemaDocumentService(JsonSchemaDocumentRepository documentRepository,
                                     JsonSchemaDocumentDefinitionService documentDefinitionService,
                                     JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService,
                                     ResourceService resourceService,
                                     UserManagementService userManagementService,
                                     AuthorizationService authorizationService,
                                     ApplicationEventPublisher applicationEventPublisher
    ) {
        this.documentRepository = documentRepository;
        this.documentDefinitionService = documentDefinitionService;
        this.documentSequenceGeneratorService = documentSequenceGeneratorService;
        this.resourceService = resourceService;
        this.userManagementService = userManagementService;
        this.authorizationService = authorizationService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Optional<JsonSchemaDocument> findBy(Document.Id documentId) {
        Optional<JsonSchemaDocument> optionalDocument = documentRepository.findById(documentId);

        if (optionalDocument.isPresent()) {
            authorizationService.requirePermission(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    Action.VIEW
                ),
                optionalDocument.get(),
                null
            );
        }
        return optionalDocument;
    }

    @Override
    public JsonSchemaDocument get(String documentId) {
        var documentOptional = runWithoutAuthorization(
            () -> findBy(JsonSchemaDocumentId.existingId(UUID.fromString(documentId))));

        JsonSchemaDocument document = documentOptional.orElseThrow(
            () -> new DocumentNotFoundException("Document not found with id " + documentId)
        );

        authorizationService.requirePermission(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.VIEW
            ),
            document,
            null
        );

        return document;
    }

    @Override
    public Page<JsonSchemaDocument> getAllByDocumentDefinitionName(Pageable pageable, String definitionName) {
        AuthorizationSpecification<JsonSchemaDocument> spec = authorizationService
            .getAuthorizationSpecification(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    Action.LIST_VIEW
                ),
                null
            );

        return documentRepository.findAll(spec.and(JsonSchemaDocumentSpecification.byDocumentDefinitionIdName(definitionName)), pageable);
    }

    // TODO: Can this be removed?
    @Override
    public Page<JsonSchemaDocument> getAll(Pageable pageable) {
        var spec = authorizationService.getAuthorizationSpecification(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.LIST_VIEW
            ),
            null
        ).or(authorizationService.getAuthorizationSpecification(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.VIEW
            ),
            null
        ));
        return documentRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public JsonSchemaDocument.CreateDocumentResultImpl createDocument(
        NewDocumentRequest newDocumentRequest
    ) {
        final JsonSchemaDocumentDefinition definition = runWithoutAuthorization(
            () -> documentDefinitionService
                .findLatestByName(newDocumentRequest.documentDefinitionName())
                .orElseThrow(
                    () -> new UnknownDocumentDefinitionException(newDocumentRequest.documentDefinitionName())
                )
        );
        final var content = JsonDocumentContent.build(newDocumentRequest.content());
        final var user = SecurityUtils.getCurrentUserLogin() != null ? SecurityUtils.getCurrentUserLogin() : SYSTEM_ACCOUNT;

        final var result = JsonSchemaDocument.create(
            definition,
            content,
            user,
            documentSequenceGeneratorService,
            JsonSchemaDocumentRelation.from(newDocumentRequest.documentRelation())
        );
        result.resultingDocument().ifPresent(
            jsonSchemaDocument -> {
                newDocumentRequest.getResources()
                    .stream()
                    .map(JsonSchemaRelatedFile::from)
                    .map(relatedFile -> relatedFile.withCreatedBy(user))
                    .forEach(jsonSchemaDocument::addRelatedFile);

                authorizationService.requirePermission(
                    new AuthorizationRequest<>(
                        JsonSchemaDocument.class,
                        Action.CREATE
                    ),
                    jsonSchemaDocument,
                    null
                );

                documentRepository.saveAndFlush(jsonSchemaDocument);
            }
        );
        return result;
    }

    @Override
    @Transactional
    public void modifyDocument(Document document, JsonNode jsonNode) {
        JsonSchemaDocument jsonSchemaDocument = (JsonSchemaDocument) document;

        authorizationService.requirePermission(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.MODIFY
            ),
            jsonSchemaDocument,
            null
        );

        final var documentRequest = ModifyDocumentRequest.create(document, jsonNode);
        final var modifyResult = runWithoutAuthorization(() -> modifyDocument(documentRequest));
        if (!modifyResult.errors().isEmpty()) {
            throw new ModifyDocumentException(modifyResult.errors());
        }
    }

    @Override
    @Transactional(timeout = 30, rollbackFor = {Exception.class})
    public synchronized JsonSchemaDocument.ModifyDocumentResultImpl modifyDocument(
        ModifyDocumentRequest request
    ) {
        final var documentId = JsonSchemaDocumentId.existingId(UUID.fromString(request.documentId()));
        final var version = JsonSchemaDocumentVersion.from(request.versionBasedOn());
        final var document = runWithoutAuthorization(
            () -> findBy(documentId)
            .orElseThrow(
                () -> new DocumentNotFoundException("Document not found with id " + request.documentId())
            )
        );

        authorizationService.requirePermission(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.MODIFY
            ),
            document,
            null
        );

        final var modifiedContent = JsonDocumentContent.build(
            document.content().asJson(),
            request.content(),
            request.jsonPatch()
        );
        var documentDefinition = runWithoutAuthorization(
            () -> documentDefinitionService.findBy(document.definitionId()).orElseThrow()
        );
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
        authorizationService
            .requirePermission(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    Action.DENY
                ),
                null,
                null
            );

        final JsonSchemaDocumentRelation jsonSchemaDocumentRelation = JsonSchemaDocumentRelation.from(
            new DocumentRelationRequest(
                UUID.fromString(documentRelation.id().toString()),
                documentRelation.relationType()
            )
        );
        runWithoutAuthorization(() ->findBy(documentId))
            .ifPresent(document -> documentRepository.save(document.addRelatedDocument(jsonSchemaDocumentRelation)));
    }

    @Override
    @Transactional
    public void assignRelatedFile(
        final Document.Id documentId,
        final RelatedFile relatedFile
    ) {
        JsonSchemaDocument document = getDocumentBy(documentId);

        authorizationService.requirePermission(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.MODIFY
            ),
            document,
            null
        );

        document.addRelatedFile(JsonSchemaRelatedFile.from(relatedFile));
        documentRepository.save(document);
    }

    @Override
    @Transactional
    public void assignResource(Document.Id documentId, UUID resourceId) {
        JsonSchemaDocument document = getDocumentBy(documentId);

        authorizationService.requirePermission(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.MODIFY
            ),
            document,
            null
        );

        assignResource(documentId, resourceId, null);
    }

    @Override
    @Transactional
    public void assignResource(Document.Id documentId, UUID resourceId, Map<String, Object> metadata) {
        JsonSchemaDocument document = getDocumentBy(documentId);

        authorizationService.requirePermission(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.MODIFY
            ),
            document,
            null
        );

        final Resource resource = resourceService.getResource(resourceId);
        document.addRelatedFile(JsonSchemaRelatedFile.from(resource).withCreatedBy(SecurityUtils.getCurrentUserLogin()), metadata);
        documentRepository.save(document);
    }

    @Override
    @Transactional
    public void removeRelatedFile(Document.Id documentId, UUID fileId) {
        JsonSchemaDocument document = getDocumentBy(documentId);

        authorizationService.requirePermission(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.MODIFY
            ),
            document,
            null
        );

        document.removeRelatedFileBy(fileId);
        documentRepository.save(document);
    }

    public JsonSchemaDocument getDocumentBy(Document.Id documentId) {
        Optional<JsonSchemaDocument> optionalDocument = findBy(documentId);

        optionalDocument.ifPresent(document -> authorizationService.requirePermission(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.VIEW
            ),
            document,
            null
        ));

        return optionalDocument
            .orElseThrow(() -> new DocumentNotFoundException("Unable to find document with ID " + documentId));
    }

    @Override
    public void removeDocuments(String documentDefinitionName) {
        List<JsonSchemaDocument> documents = AuthorizationContext
            .runWithoutAuthorization(
                () -> getAllByDocumentDefinitionName(Pageable.unpaged(), documentDefinitionName).toList());
        if (!documents.isEmpty()) {
            documents.forEach(document -> {
                    authorizationService.requirePermission(
                        new AuthorizationRequest<>(
                            JsonSchemaDocument.class,
                            Action.DELETE
                        ),
                        document,
                        null
                    );
                    document.removeAllRelatedFiles();
                });
            documentRepository.saveAll(documents);
            documentRepository.deleteAll(documents);
            documentSequenceGeneratorService.deleteSequenceRecordBy(documentDefinitionName);
        }
    }

    @Override
    public void claim(UUID documentId) {
        JsonSchemaDocument document = runWithoutAuthorization(
            () -> getDocumentBy(
                JsonSchemaDocumentId.existingId(documentId)
            )
        );

        // TODO: Expand authorizationRequest to accept a list of actions, which is handled like OR
        try {
            authorizationService
                .requirePermission(
                    new AuthorizationRequest<>(
                        JsonSchemaDocument.class,
                        Action.CLAIM
                    ),
                    document,
                    null
                );
        } catch (Exception e) {
            authorizationService
                .requirePermission(
                    new AuthorizationRequest<>(
                        JsonSchemaDocument.class,
                        Action.ASSIGN
                    ),
                    document,
                    null
                );
        }
        var assignee = userManagementService.getCurrentUser();

        document.setAssignee(assignee.getId(), assignee.getFullName());
        documentRepository.save(document);

        // Publish an event to update the audit log
        publishDocumentAssigneeChangedEvent(documentId, assignee.getFullName());
    }

    @Override
    public void assignUserToDocument(UUID documentId, String assigneeId) {
        JsonSchemaDocument document = runWithoutAuthorization(
                () -> getDocumentBy(
                    JsonSchemaDocumentId.existingId(documentId)
                )
            );

        authorizationService
            .requirePermission(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    Action.ASSIGN
                ),
                document,
                null
            );

        var assignee = runWithoutAuthorization(() -> userManagementService.findById(assigneeId));
        if (assignee == null) {
            logger.debug("Cannot set assignee for the invalid user id {}", assigneeId);
            throw new IllegalArgumentException("Cannot set assignee for the invalid user id " + assigneeId);
        }

        document.setAssignee(assigneeId, assignee.getFullName());
        documentRepository.save(document);

        // Publish an event to update the audit log
        publishDocumentAssigneeChangedEvent(documentId, assignee.getFullName());
    }

    @Override
    public void unassignUserFromDocument(UUID documentId) {
        JsonSchemaDocument document = runWithoutAuthorization(
            () -> getDocumentBy(JsonSchemaDocumentId.existingId(documentId))
        );

        authorizationService
            .requirePermission(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    Action.ASSIGN
                ),
                document,
                null
            );

        document.unassign();
        documentRepository.save(document);
        applicationEventPublisher.publishEvent(
            new DocumentUnassignedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                documentId
            )
        );
    }

    private void publishDocumentAssigneeChangedEvent(UUID documentId, String assigneeFullName) {
        applicationEventPublisher.publishEvent(
            new DocumentAssigneeChangedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                documentId,
                assigneeFullName
            )
        );
    }

    @Override
    public List<NamedUser> getCandidateUsers(Document.Id documentId) {
        // TODO: Determine permissions
        // TODO: ASSIGN

        return userManagementService.findNamedUserByRoles(getDocumentRoles(documentId));
    }

    private Set<String> getDocumentRoles(Document.Id documentId) {
        // TODO determine permissions (and thus roles) based on document ID
        var document = AuthorizationContext.runWithoutAuthorization(() -> get(documentId.toString()));
        return documentDefinitionService.getDocumentDefinitionRoles(document.definitionId().name());
    }

}
