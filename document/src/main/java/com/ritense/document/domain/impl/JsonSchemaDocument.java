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

package com.ritense.document.domain.impl;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentTrue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.InternalCaseStatus;
import com.ritense.document.domain.RelatedFile;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentCreatedEvent;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentModifiedEvent;
import com.ritense.document.domain.impl.relation.JsonSchemaDocumentRelation;
import com.ritense.document.domain.relation.DocumentRelation;
import com.ritense.document.domain.validation.DocumentContentValidationResult;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.document.service.result.DocumentResult;
import com.ritense.document.service.result.ModifyDocumentResult;
import com.ritense.document.service.result.error.DocumentOperationError;
import com.ritense.valtimo.contract.Constants;
import com.ritense.valtimo.contract.audit.utils.AuditHelper;
import com.ritense.valtimo.contract.document.event.DocumentRelatedFileAddedEvent;
import com.ritense.valtimo.contract.document.event.DocumentRelatedFileRemovedEvent;
import com.ritense.valtimo.contract.utils.RequestHelper;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;

@Entity
@Table(
    name = "json_schema_document",
    indexes = {
        @Index(name = "document_definition_name_index", columnList = "document_definition_name"),
        @Index(name = "created_on_index", columnList = "created_on"),
        @Index(name = "created_by_index", columnList = "created_by"),
        @Index(name = "sequence_index", columnList = "sequence")
    }
)
@DynamicUpdate
public class JsonSchemaDocument extends AbstractAggregateRoot<JsonSchemaDocument>
    implements Document, Persistable<JsonSchemaDocumentId> {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDocument.class);
    @EmbeddedId
    private JsonSchemaDocumentId id;

    @Embedded
    private JsonDocumentContent content;

    @Embedded
    private JsonSchemaDocumentDefinitionId documentDefinitionId;

    @Version
    private Integer version;

    @Column(name = "created_on", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "modified_on", columnDefinition = "DATETIME")
    private LocalDateTime modifiedOn = null;

    @Column(name = "created_by", columnDefinition = "VARCHAR(255)")
    private String createdBy;

    @Column(name = "sequence", columnDefinition = "BIGINT")
    private Long sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        {
            @JoinColumn(name="case_definition_name", referencedColumnName="case_definition_name"),
            @JoinColumn(name="internal_case_status_key", referencedColumnName="internal_case_status_key")
        }
    )
    private InternalCaseStatus internalStatus;

    @Column(name = "assignee_id", columnDefinition = "varchar(64)")
    private String assigneeId;

    @Column(name = "assignee_full_name", columnDefinition = "varchar(255)")
    private String assigneeFullName;

    @Type(JsonType.class)
    @Column(name = "document_relations", columnDefinition = "json")
    private Set<JsonSchemaDocumentRelation> documentRelations = new HashSet<>();

    @Type(JsonType.class)
    @Column(name = "related_files", columnDefinition = "json")
    private Set<JsonSchemaRelatedFile> relatedFiles = new HashSet<>();

    private JsonSchemaDocument(
        final JsonSchemaDocumentId id,
        final JsonDocumentContent content,
        final JsonSchemaDocumentDefinition documentDefinition,
        final String createdBy,
        final Long sequence,
        final JsonSchemaDocumentRelation documentRelation
    ) {
        assertArgumentNotNull(id, "id is required");
        assertArgumentNotNull(content, "content is required");
        assertArgumentNotNull(documentDefinition, "documentDefinition is required");
        assertArgumentNotNull(createdBy, "createdBy is required");
        assertArgumentNotNull(sequence, "sequence is required");
        assertArgumentTrue(sequence > 0, "Document sequence must be positive");

        this.id = id;
        this.content = content;
        this.documentDefinitionId = documentDefinition.id();
        this.createdOn = LocalDateTime.now();
        this.createdBy = createdBy;
        this.sequence = sequence;

        addRelatedDocument(documentRelation);

        registerEvent(
            new JsonSchemaDocumentCreatedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                this.createdOn,
                this.createdBy,
                this.id,
                this.documentDefinitionId
            )
        );
    }

    JsonSchemaDocument() {
    }

    public static CreateDocumentResultImpl create(
        final JsonSchemaDocumentDefinition definition,
        final JsonDocumentContent content,
        final String createdBy,
        final DocumentSequenceGeneratorService documentSequenceGeneratorService,
        final JsonSchemaDocumentRelation documentRelation
    ) {
        assertArgumentNotNull(definition, "definition is required");
        assertArgumentNotNull(content, "content is required");
        assertArgumentNotNull(createdBy, "createdBy is required");

        final DocumentContentValidationResult result = definition.validate(content);

        if (!result.passedValidation()) {
            List<DocumentOperationError> documentContentValidationErrors = new ArrayList<>(result.validationErrors());
            return new CreateDocumentResultImpl(documentContentValidationErrors);
        }

        final var sequence = documentSequenceGeneratorService.next(definition.id());

        final var document = new JsonSchemaDocument(
            JsonSchemaDocumentId.newId(UUID.randomUUID()),
            (JsonDocumentContent) result.content(),
            definition,
            createdBy,
            sequence,
            documentRelation
        );
        return new CreateDocumentResultImpl(document);
    }

    /**
     * Sets this document's content to the given one (super-set value), but only if it passes validation.
     *
     * <p><b>Note: modification has to go through the service for prevention of write-after-read's.</b></p>
     * Note: Distributed locking mechanism (currently assumes only one application instance doing a document modification)
     *
     * @param modifiedContent The new (unvalidated) content
     * @return Object representing the result of the operation (either resulting document or errors)
     */
    public ModifyDocumentResultImpl applyModifiedContent(
        final JsonDocumentContent modifiedContent,
        final JsonSchemaDocumentDefinition documentDefinition
    ) {
        assertArgumentNotNull(modifiedContent, "modifiedContent is required");

        if (!content.equals(modifiedContent)) {
            final var result = documentDefinition.validate(modifiedContent);
            if (!result.passedValidation()) {
                List<DocumentOperationError> documentContentValidationErrors = new ArrayList<>(result.validationErrors());
                return new ModifyDocumentResultImpl(documentContentValidationErrors);
            }
            var originalContent = this.content;
            this.content = modifiedContent;
            this.modifiedOn = LocalDateTime.now();

            //Full re-Diff
            final var diff = originalContent.diff(content);
            final List<JsonSchemaDocumentFieldChangedEvent> changes = StreamSupport
                .stream(diff.spliterator(), false)
                .map(JsonSchemaDocumentFieldChangedEvent::fromJsonNode)
                .toList();

            registerEvent(
                new JsonSchemaDocumentModifiedEvent(
                    UUID.randomUUID(),
                    RequestHelper.getOrigin(),
                    LocalDateTime.now(),
                    AuditHelper.getActor(),
                    id(),
                    changes
                )
            );
        }
        return new ModifyDocumentResultImpl(this);
    }

    public JsonSchemaDocument addRelatedDocument(final JsonSchemaDocumentRelation documentRelation) {
        if (documentRelation != null) {
            this.documentRelations.add(documentRelation);
        }
        return this;
    }

    public void addRelatedFile(final JsonSchemaRelatedFile relatedFile) {
        addRelatedFile(relatedFile, null);
    }

    public void addRelatedFile(final JsonSchemaRelatedFile relatedFile, Map<String, Object> metadata) {
        assertArgumentNotNull(relatedFile, "relatedFile is required");
        if (this.relatedFiles.add(relatedFile)) {
            registerEvent(
                new DocumentRelatedFileAddedEvent(
                    UUID.randomUUID(),
                    RequestHelper.getOrigin(),
                    LocalDateTime.now(),
                    AuditHelper.getActor(),
                    id.getId(),
                    relatedFile.getFileId(),
                    relatedFile.getFileName(),
                    metadata
                )
            );
        } else {
            logger.warn("Related file not added");
        }
    }

    public void removeRelatedFileBy(final UUID fileId) {
        assertArgumentNotNull(fileId, "fileId is required");
        final JsonSchemaRelatedFile relatedFile = relatedFiles
            .stream()
            .filter(jsonSchemaRelatedFile -> jsonSchemaRelatedFile.getFileId().equals(fileId))
            .findAny()
            .orElseThrow();

        if (relatedFiles.remove(relatedFile)) {
            registerEvent(
                new DocumentRelatedFileRemovedEvent(
                    UUID.randomUUID(),
                    RequestHelper.getOrigin(),
                    LocalDateTime.now(),
                    AuditHelper.getActor(),
                    id.getId(),
                    relatedFile.getFileId(),
                    relatedFile.getFileName()
                )
            );
        } else {
            logger.warn("Related file not removed");
        }
    }

    public void removeAllRelatedFiles() {
        relatedFiles.forEach(file -> removeRelatedFileBy(file.getFileId()));
    }

    public void setAssignee(String id, String fullName) {
        this.assigneeId = id;
        this.assigneeFullName = fullName;
    }

    public void unassign() {
        this.assigneeId = null;
        this.assigneeFullName = null;
    }

    public void setInternalStatus(@Nullable InternalCaseStatus internalCaseStatus) {
        if (internalCaseStatus != null && !internalCaseStatus.getId().getKey().matches(Constants.KEY_REGEX)) {
            throw new IllegalArgumentException("Invalid status key: '" + internalCaseStatus.getId().getKey() + "'.");
        }
        this.internalStatus = internalCaseStatus;
    }

    @Override
    public JsonSchemaDocumentId id() {
        return id;
    }

    @Override
    public LocalDateTime createdOn() {
        return createdOn;
    }

    @Override
    public Optional<LocalDateTime> modifiedOn() {
        return Optional.ofNullable(modifiedOn);
    }

    @Override
    public JsonDocumentContent content() {
        return content;
    }

    @Override
    public JsonSchemaDocumentDefinitionId definitionId() {
        return documentDefinitionId;
    }

    public void setDefinitionId(JsonSchemaDocumentDefinitionId documentDefinitionId) {
        this.documentDefinitionId = documentDefinitionId;
    }

    @Override
    @Nullable
    public String internalStatus() {
        if (internalStatus == null) {
            return null;
        } else {
            return internalStatus.getId().getKey();
        }
    }

    @Override
    public String assigneeId() {
        return assigneeId;
    }

    @Override
    public String assigneeFullName() {
        return assigneeFullName;
    }

    @Override
    public Integer version() {
        return version;
    }

    @Override
    public String createdBy() {
        return createdBy;
    }

    @Override
    public Long sequence() {
        return sequence;
    }

    @Override
    public Set<? extends DocumentRelation> relations() {
        return documentRelations;
    }

    @Override
    public Set<? extends RelatedFile> relatedFiles() {
        return relatedFiles;
    }

    @Override
    @JsonIgnore
    @Nonnull
    public JsonSchemaDocumentId getId() {
        return id;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return id.isNew();
    }

    ////////////////////////////////////////
    //// DocumentResult IMPLEMENTATIONS ////
    ////////////////////////////////////////

    public static class CreateDocumentResultImpl extends AbstractDocumentResult implements CreateDocumentResult {
        CreateDocumentResultImpl(JsonSchemaDocument resultingDocument) {
            super(resultingDocument);
        }

        CreateDocumentResultImpl(List<DocumentOperationError> errors) {
            super(errors);
        }
    }

    public static class ModifyDocumentResultImpl extends AbstractDocumentResult implements ModifyDocumentResult {
        public ModifyDocumentResultImpl(JsonSchemaDocument resultingDocument) {
            super(resultingDocument);
        }

        public ModifyDocumentResultImpl(List<DocumentOperationError> errors) {
            super(errors);
        }
    }

    private abstract static class AbstractDocumentResult implements DocumentResult {
        private final JsonSchemaDocument resultingDocument;
        private final List<DocumentOperationError> errors;

        private AbstractDocumentResult(JsonSchemaDocument resultingDocument, List<DocumentOperationError> errors) {
            this.resultingDocument = resultingDocument;
            this.errors = errors != null ? errors : Collections.emptyList();
        }

        AbstractDocumentResult(JsonSchemaDocument resultingDocument) {
            this(resultingDocument, Collections.emptyList());
        }

        AbstractDocumentResult(List<DocumentOperationError> errors) {
            this(null, errors);
        }

        @Override
        public Optional<JsonSchemaDocument> resultingDocument() {
            return Optional.ofNullable(resultingDocument);
        }

        @Override
        public List<DocumentOperationError> errors() {
            return Collections.unmodifiableList(errors);
        }
    }

}
