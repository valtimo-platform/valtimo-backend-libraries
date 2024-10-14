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

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.document.repository.impl.specification.JsonSchemaDocumentSpecificationHelper.byDocumentDefinitionIdName;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.ASSIGN;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.ASSIGNABLE;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.CLAIM;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.CREATE;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.DELETE;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.MODIFY;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW_LIST;
import static com.ritense.logging.LoggingContextKt.withLoggingContext;
import static com.ritense.valtimo.contract.Constants.SYSTEM_ACCOUNT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.DelegateUserEntityAuthorizationRequest;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.authorization.role.Role;
import com.ritense.authorization.specification.AuthorizationSpecification;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.RelatedFile;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.JsonSchemaRelatedFile;
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation;
import com.ritense.document.domain.impl.request.DocumentRelationRequest;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.relation.DocumentRelation;
import com.ritense.document.event.DocumentAssigned;
import com.ritense.document.event.DocumentAssigneeChangedEvent;
import com.ritense.document.event.DocumentCreated;
import com.ritense.document.event.DocumentDeleted;
import com.ritense.document.event.DocumentStatusChanged;
import com.ritense.document.event.DocumentUnassigned;
import com.ritense.document.event.DocumentUnassignedEvent;
import com.ritense.document.event.DocumentUpdated;
import com.ritense.document.event.DocumentViewed;
import com.ritense.document.event.DocumentsListed;
import com.ritense.document.exception.DocumentNotFoundException;
import com.ritense.document.exception.ModifyDocumentException;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.InternalCaseStatusService;
import com.ritense.logging.LoggableResource;
import com.ritense.outbox.OutboxService;
import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.audit.utils.AuditHelper;
import com.ritense.valtimo.contract.authentication.NamedUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.resource.Resource;
import com.ritense.valtimo.contract.utils.RequestHelper;
import com.ritense.valtimo.contract.utils.SecurityUtils;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JsonSchemaDocumentService implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDocumentService.class);

    private final JsonSchemaDocumentRepository documentRepository;
    private final JsonSchemaDocumentDefinitionService documentDefinitionService;
    private final JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService;

    private final UserManagementService userManagementService;
    private final ResourceService resourceService;

    private final AuthorizationService authorizationService;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final OutboxService outboxService;

    private final ObjectMapper objectMapper;

    private final InternalCaseStatusService internalCaseStatusService;

    public JsonSchemaDocumentService(
        JsonSchemaDocumentRepository documentRepository,
        JsonSchemaDocumentDefinitionService documentDefinitionService,
        JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService,
        @Nullable ResourceService resourceService,
        UserManagementService userManagementService,
        AuthorizationService authorizationService,
        ApplicationEventPublisher applicationEventPublisher,
        OutboxService outboxService,
        ObjectMapper objectMapper,
        InternalCaseStatusService internalCaseStatusService
    ) {
        this.documentRepository = documentRepository;
        this.documentDefinitionService = documentDefinitionService;
        this.documentSequenceGeneratorService = documentSequenceGeneratorService;
        this.resourceService = resourceService;
        this.userManagementService = userManagementService;
        this.authorizationService = authorizationService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
        this.internalCaseStatusService = internalCaseStatusService;
    }

    @Override
    public Optional<JsonSchemaDocument> findBy(
        @LoggableResource(resourceType = JsonSchemaDocument.class) Document.Id documentId
    ) {
        Optional<JsonSchemaDocument> optionalDocument = documentRepository.findById(documentId);

        if (optionalDocument.isPresent()) {
            JsonSchemaDocument document = optionalDocument.get();
            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    VIEW,
                    document
                )
            );

            outboxService.send(() ->
                new DocumentViewed(
                    document.id().toString(),
                    objectMapper.valueToTree(document)
                )
            );
        }
        return optionalDocument;
    }

    @Override
    public JsonSchemaDocument get(
        @LoggableResource(resourceType = JsonSchemaDocument.class) String documentId
    ) {
        var documentOptional = runWithoutAuthorization(
            () -> findBy(JsonSchemaDocumentId.existingId(UUID.fromString(documentId))));

        JsonSchemaDocument document = documentOptional.orElseThrow(
            () -> new DocumentNotFoundException("Document not found with id " + documentId)
        );

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                VIEW,
                document
            )
        );

        return document;
    }

    @Override
    public Page<JsonSchemaDocument> getAllByDocumentDefinitionName(
        Pageable pageable,
        @LoggableResource("documentDefinitionName") String definitionName
    ) {
        AuthorizationSpecification<JsonSchemaDocument> spec = authorizationService
            .getAuthorizationSpecification(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    VIEW_LIST
                ),
                null
            );

        Page<JsonSchemaDocument> documentPage = documentRepository.findAll(
            spec.and(byDocumentDefinitionIdName(definitionName)), pageable);

        outboxService.send(() ->
            new DocumentsListed(
                objectMapper.valueToTree(documentPage.getContent())
            )
        );

        return documentPage;
    }

    // TODO: Can this be removed?
    @Override
    public Page<JsonSchemaDocument> getAll(Pageable pageable) {
        var spec = authorizationService.getAuthorizationSpecification(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                VIEW_LIST
            ),
            null
        ).or(authorizationService.getAuthorizationSpecification(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                VIEW
            ),
            null
        ));
        Page<JsonSchemaDocument> documentPage = documentRepository.findAll(spec, pageable);

        outboxService.send(() ->
            new DocumentsListed(
                objectMapper.valueToTree(documentPage.getContent())
            )
        );

        return documentPage;
    }

    @Override
    public JsonSchemaDocument.CreateDocumentResultImpl createDocument(
        NewDocumentRequest newDocumentRequest
    ) {
        return withLoggingContext("documentDefinitionName", newDocumentRequest.documentDefinitionName(), () -> {
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
                        new EntityAuthorizationRequest<>(
                            JsonSchemaDocument.class,
                            CREATE,
                            jsonSchemaDocument
                        )
                    );

                    documentRepository.save(jsonSchemaDocument);

                    outboxService.send(() ->
                        new DocumentCreated(
                            jsonSchemaDocument.id().toString(),
                            objectMapper.valueToTree(jsonSchemaDocument)
                        )
                    );
                }
            );
            return result;
        });
    }

    @Override
    public void modifyDocument(Document document, JsonNode jsonNode) {
        withLoggingContext(JsonSchemaDocument.class, document.id().toString(), () -> {
            JsonSchemaDocument jsonSchemaDocument = (JsonSchemaDocument) document;

            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    MODIFY,
                    jsonSchemaDocument
                )
            );

            final var documentRequest = ModifyDocumentRequest.create(document, jsonNode);
            final var modifyResult = runWithoutAuthorization(() -> modifyDocument(documentRequest));
            if (!modifyResult.errors().isEmpty()) {
                var exception = new ModifyDocumentException(modifyResult.errors());
                logger.error("Document could not be modified", exception);
                throw exception;
            }
        });
    }

    @Override
    @Transactional(timeout = 30, rollbackFor = {Exception.class})
    public JsonSchemaDocument.ModifyDocumentResultImpl modifyDocument(
        ModifyDocumentRequest request
    ) {
        return withLoggingContext(JsonSchemaDocument.class, request.documentId(), () -> {
            final var documentId = JsonSchemaDocumentId.existingId(UUID.fromString(request.documentId()));
            final var document = runWithoutAuthorization(
                () -> findBy(documentId)
                    .orElseThrow(
                        () -> new DocumentNotFoundException("Document not found with id " + request.documentId())
                    )
            );

            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    MODIFY,
                    document
                )
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
                documentDefinition
            );

            result.resultingDocument().ifPresent(modifiedDocument -> {
                documentRepository.save(modifiedDocument);
                outboxService.send(() ->
                    new DocumentUpdated(
                        modifiedDocument.id().toString(),
                        objectMapper.valueToTree(modifiedDocument)
                    )
                );
            });

            return result;
        });
    }

    @Override
    public void assignDocumentRelation(
        @LoggableResource(resourceType = JsonSchemaDocument.class) Document.Id documentId,
        DocumentRelation documentRelation
    ) {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.deny()
            )
        );

        final JsonSchemaDocumentRelation jsonSchemaDocumentRelation = JsonSchemaDocumentRelation.from(
            new DocumentRelationRequest(
                UUID.fromString(documentRelation.id().toString()),
                documentRelation.relationType()
            )
        );
        runWithoutAuthorization(() -> findBy(documentId))
            .ifPresent(document -> documentRepository.save(document.addRelatedDocument(jsonSchemaDocumentRelation)));
    }

    @Override
    public void assignRelatedFile(
        @LoggableResource(resourceType = JsonSchemaDocument.class) final Document.Id documentId,
        final RelatedFile relatedFile
    ) {
        JsonSchemaDocument document = getDocumentBy(documentId);

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                MODIFY,
                document
            )
        );

        document.addRelatedFile(JsonSchemaRelatedFile.from(relatedFile));
        documentRepository.save(document);
    }

    @Override
    public void assignResource(
        @LoggableResource(resourceType = JsonSchemaDocument.class) Document.Id documentId,
        UUID resourceId
    ) {
        JsonSchemaDocument document = getDocumentBy(documentId);

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                MODIFY,
                document
            )
        );

        assignResource(documentId, resourceId, null);
    }

    @Override
    public void assignResource(
        @LoggableResource(resourceType = JsonSchemaDocument.class) Document.Id documentId,
        UUID resourceId,
        Map<String, Object> metadata
    ) {
        JsonSchemaDocument document = getDocumentBy(documentId);

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                MODIFY,
                document
            )
        );

        if (resourceService == null) {
            throw new RuntimeException("No ResourceService implementation was provided. Resource assignment is unavailable.");
        }

        final Resource resource = resourceService.getResource(resourceId);
        document.addRelatedFile(
            JsonSchemaRelatedFile.from(resource).withCreatedBy(SecurityUtils.getCurrentUserLogin()), metadata);
        documentRepository.save(document);
    }

    @Override
    public void removeRelatedFile(
        @LoggableResource(resourceType = JsonSchemaDocument.class) Document.Id documentId,
        UUID fileId
    ) {
        JsonSchemaDocument document = getDocumentBy(documentId);

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                MODIFY,
                document
            )
        );

        document.removeRelatedFileBy(fileId);
        documentRepository.save(document);
    }

    public JsonSchemaDocument getDocumentBy(
        @LoggableResource(resourceType = JsonSchemaDocument.class) Document.Id documentId
    ) {
        Optional<JsonSchemaDocument> optionalDocument = findBy(documentId);

        optionalDocument.ifPresent(document -> authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                VIEW,
                document
            )
        ));

        return optionalDocument
            .orElseThrow(() -> new DocumentNotFoundException("Unable to find document with ID " + documentId));
    }

    @Override
    public void removeDocuments(
        @LoggableResource("documentDefinitionName") String documentDefinitionName
    ) {
        List<JsonSchemaDocument> documents = AuthorizationContext
            .runWithoutAuthorization(
                () -> getAllByDocumentDefinitionName(Pageable.unpaged(), documentDefinitionName).toList());
        if (!documents.isEmpty()) {
            documents.forEach(document -> {
                authorizationService.requirePermission(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocument.class,
                        DELETE,
                        document
                    )
                );
                document.removeAllRelatedFiles();
            });
            documentRepository.saveAll(documents);
            documentRepository.deleteAll(documents);
            documents.forEach(document -> outboxService.send(() ->
                new DocumentDeleted(
                    document.id().toString()
                )
            ));
            documentSequenceGeneratorService.deleteSequenceRecordBy(documentDefinitionName);
        }
    }

    @Override
    public void claim(
        @LoggableResource(resourceType = JsonSchemaDocument.class) UUID documentId
    ) {
        JsonSchemaDocument document = runWithoutAuthorization(
            () -> getDocumentBy(
                JsonSchemaDocumentId.existingId(documentId)
            )
        );

        // TODO: Expand authorizationRequest to accept a list of actions, which is handled like OR
        try {
            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    CLAIM,
                    document
                )
            );
        } catch (Exception e) {
            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    ASSIGN,
                    document
                )
            );
            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    ASSIGNABLE,
                    document
                )
            );
        }
        var assignee = userManagementService.getCurrentUser();

        document.setAssignee(assignee.getUserIdentifier(), assignee.getFullName());
        documentRepository.save(document);

        // Publish an event to update the audit log
        publishDocumentAssigneeChangedEvent(documentId, assignee.getFullName());

        outboxService.send(() ->
            new DocumentAssigned(
                document.id().toString(),
                objectMapper.valueToTree(document)
            )
        );
    }

    @Override
    public void assignUserToDocument(
        @LoggableResource(resourceType = JsonSchemaDocument.class) UUID documentId,
        String assigneeId
    ) {
        JsonSchemaDocument document = runWithoutAuthorization(
            () -> getDocumentBy(
                JsonSchemaDocumentId.existingId(documentId)
            )
        );

        var assignee = runWithoutAuthorization(() -> userManagementService.findById(assigneeId));
        if (assignee == null) {
            logger.debug("Cannot set assignee for the invalid user id {}", assigneeId);
            throw new IllegalArgumentException("Cannot set assignee for the invalid user id " + assigneeId);
        }
        if (assignee.getUserIdentifier().equals(userManagementService.getCurrentUser().getUserIdentifier())) {
            try {
                authorizationService.requirePermission(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocument.class,
                        CLAIM,
                        document
                    )
                );
            } catch (AccessDeniedException e) {
                authorizationService.requirePermission(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocument.class,
                        ASSIGN,
                        document
                    )
                );
                authorizationService.requirePermission(
                    new EntityAuthorizationRequest<>(
                        JsonSchemaDocument.class,
                        ASSIGNABLE,
                        document
                    )
                );
            }
        } else {
            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    ASSIGN,
                    document
                )
            );
            authorizationService.requirePermission(
                new DelegateUserEntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    ASSIGNABLE,
                    assignee.getUserIdentifier(),
                    document
                )
            );
        }

        document.setAssignee(assignee.getUserIdentifier(), assignee.getFullName());
        documentRepository.save(document);

        // Publish an event to update the audit log
        publishDocumentAssigneeChangedEvent(documentId, assignee.getFullName());

        outboxService.send(() ->
            new DocumentAssigned(
                document.id().toString(),
                objectMapper.valueToTree(document)
            )
        );
    }

    @Override
    public void unassignUserFromDocument(
        @LoggableResource(resourceType = JsonSchemaDocument.class) UUID documentId
    ) {
        JsonSchemaDocument document = runWithoutAuthorization(
            () -> getDocumentBy(JsonSchemaDocumentId.existingId(documentId))
        );

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                ASSIGN,
                document
            )
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

        outboxService.send(() ->
            new DocumentUnassigned(
                document.id().toString(),
                objectMapper.valueToTree(document)
            )
        );
    }

    @Override
    public void setInternalStatus(
        @LoggableResource(resourceType = JsonSchemaDocument.class) Document.Id documentId,
        @Nullable String internalStatusKey
    ) {
        JsonSchemaDocument document = runWithoutAuthorization(
            () -> getDocumentBy(documentId)
        );

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                MODIFY,
                document
            )
        );

        var internalCaseStatus = internalStatusKey != null ? internalCaseStatusService.get(
            document.definitionId().name(),
            internalStatusKey
        ) : null;
        document.setInternalStatus(internalCaseStatus);

        documentRepository.save(document);

        outboxService.send(() ->
            new DocumentStatusChanged(
                document.id().toString(),
                objectMapper.valueToTree(document)
            )
        );
    }

    private void publishDocumentAssigneeChangedEvent(
        @LoggableResource(resourceType = JsonSchemaDocument.class) UUID documentId,
        String assigneeFullName
    ) {
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
    public List<NamedUser> getCandidateUsers(
        @LoggableResource(resourceType = JsonSchemaDocument.class) Document.Id documentId
    ) {
        var document = AuthorizationContext.runWithoutAuthorization(() -> get(documentId.toString()));
        // TODO: Write tests
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                ASSIGN,
                document
            )
        );

        Set<String> authorizedRoles = authorizationService.getAuthorizedRoles(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                ASSIGNABLE,
                document
            )
        ).stream().map(Role::getKey).collect(Collectors.toSet());

        return userManagementService.findNamedUserByRoles(authorizedRoles);
    }

    @Override
    public List<NamedUser> getCandidateUsers(List<Document.Id> documentIds) {
        List<JsonSchemaDocument> documents = runWithoutAuthorization(() ->
            documentRepository.findAllById(documentIds)
        );
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                ASSIGN,
                documents
            )
        );
        if (documents.size() != documentIds.size()) {
            var missingDocumentIds = documentIds.stream()
                .filter(documentId -> documents.stream().noneMatch(document -> document.id().equals(documentId)))
                .map(Document.Id::toString)
                .collect(Collectors.joining(", "));
            throw new DocumentNotFoundException("Document(s) not found with id(s) " + missingDocumentIds);
        }

        Set<String> authorizedRoles = authorizationService.getAuthorizedRoles(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                ASSIGNABLE,
                documents
            )
        ).stream().map(Role::getKey).collect(Collectors.toSet());

        return userManagementService.findNamedUserByRoles(authorizedRoles);
    }

    public void assignUserToDocuments(List<UUID> documentIds, String assigneeId) {
        var documentIdObjects = documentIds.stream()
            .map(documentId -> (Document.Id) JsonSchemaDocumentId.existingId(documentId))
            .toList();
        List<JsonSchemaDocument> documents = runWithoutAuthorization(() ->
            documentRepository.findAllById(documentIdObjects)
        );

        var assignee = runWithoutAuthorization(() -> userManagementService.findById(assigneeId));
        if (assignee == null) {
            logger.debug("Cannot set assignee for the invalid user id {}", assigneeId);
            throw new IllegalArgumentException("Cannot set assignee for the invalid user id " + assigneeId);
        }
        var assigneeIdentifier = assignee.getUserIdentifier();
        if (assigneeIdentifier.equals(userManagementService.getCurrentUser().getUserIdentifier())) {
            documents.forEach(document -> {
                try {
                    authorizationService.requirePermission(
                        new EntityAuthorizationRequest<>(
                            JsonSchemaDocument.class,
                            CLAIM,
                            document
                        )
                    );
                } catch (AccessDeniedException e) {
                    authorizationService.requirePermission(
                        new EntityAuthorizationRequest<>(
                            JsonSchemaDocument.class,
                            ASSIGN,
                            document
                        )
                    );
                    authorizationService.requirePermission(
                        new EntityAuthorizationRequest<>(
                            JsonSchemaDocument.class,
                            ASSIGNABLE,
                            document
                        )
                    );
                }

                document.setAssignee(assigneeIdentifier, assignee.getFullName());
            });
        } else {
            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    ASSIGN,
                    documents
                )
            );
            authorizationService.requirePermission(
                new DelegateUserEntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    ASSIGNABLE,
                    assigneeIdentifier,
                    documents
                )
            );

            documents.forEach(document -> document.setAssignee(assigneeIdentifier, assignee.getFullName()));
        }
        documentRepository.saveAll(documents);

        // Publish an event to update the audit log
        documents.forEach(document -> {
            publishDocumentAssigneeChangedEvent(document.id().getId(), assignee.getFullName());

            outboxService.send(() ->
                new DocumentAssigned(
                    document.id().toString(),
                    objectMapper.valueToTree(document)
                )
            );
        });
    }
}
