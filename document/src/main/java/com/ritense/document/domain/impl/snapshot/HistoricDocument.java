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

package com.ritense.document.domain.impl.snapshot;

import com.ritense.document.domain.Document;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.RelatedFile;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.JsonSchemaDocumentVersion;
import com.ritense.document.domain.relation.DocumentRelation;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Embeddable
public class HistoricDocument implements Document {

    @Embedded
    private JsonSchemaDocumentId id;

    @Embedded
    private JsonDocumentContent content;

    @Embedded
    private JsonSchemaDocumentDefinitionId documentDefinitionId;

    @Transient
    private DocumentDefinition documentDefinition;

    @Transient
    private JsonSchemaDocumentVersion version;

    @Column(name = "document_created_on", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "document_modified_on", columnDefinition = "DATETIME")
    private LocalDateTime modifiedOn = null;

    @Column(name = "document_created_by", columnDefinition = "VARCHAR(255)")
    private String createdBy;

    @Column(name = "document_sequence", columnDefinition = "BIGINT")
    private Long sequence;

    @Column(name = "document_assignee_id", columnDefinition = "VARCHAR(64)")
    private String assigneeId;

    @Column(name = "document_assignee_full_name", columnDefinition = "VARCHAR(255)")
    private String assigneeFullName;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "document_relations", columnDefinition = "json")
    private Set<? extends DocumentRelation> documentRelations = new HashSet<>();

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "document_related_files", columnDefinition = "json")
    private Set<? extends RelatedFile> relatedFiles = new HashSet<>();

    public HistoricDocument(
        final JsonSchemaDocument document,
        final JsonSchemaDocumentDefinition documentDefinition
    ) {
        this.id = document.id();
        this.content = new JsonDocumentContent(document.content());
        this.documentDefinitionId = document.definitionId();
        this.documentDefinition = documentDefinition;
        this.version = document.version();
        this.createdOn = document.createdOn();
        this.modifiedOn = document.modifiedOn().orElse(null);
        this.createdBy = document.createdBy();
        this.sequence = document.sequence();
        this.assigneeId = document.assigneeId();
        this.assigneeFullName = document.assigneeFullName();
        this.documentRelations = document.relations();
        this.relatedFiles = document.relatedFiles();
    }

    public HistoricDocument() {
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
    public JsonSchemaDocumentVersion version() {
        return version;
    }

    @Override
    public JsonSchemaDocumentDefinitionId definitionId() {
        return documentDefinitionId;
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
    public String assigneeId() {
        return assigneeId;
    }

    @Override
    public String assigneeFullName() {
        return assigneeFullName;
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
    public String tenantId() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricDocument that = (HistoricDocument) o;
        return Objects.equals(id, that.id) && Objects.equals(content, that.content) && Objects.equals(documentDefinitionId, that.documentDefinitionId) && Objects.equals(documentDefinition, that.documentDefinition) && Objects.equals(version, that.version) && Objects.equals(createdOn, that.createdOn) && Objects.equals(modifiedOn, that.modifiedOn) && Objects.equals(createdBy, that.createdBy) && Objects.equals(sequence, that.sequence) && Objects.equals(documentRelations, that.documentRelations) && Objects.equals(relatedFiles, that.relatedFiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, documentDefinitionId, documentDefinition, version, createdOn, modifiedOn, createdBy, sequence, documentRelations, relatedFiles);
    }
}
