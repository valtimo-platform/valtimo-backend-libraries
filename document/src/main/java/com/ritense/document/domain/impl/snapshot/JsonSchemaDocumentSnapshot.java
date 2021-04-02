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

package com.ritense.document.domain.impl.snapshot;

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.snapshot.DocumentSnapshot;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "json_schema_document_snapshot")
public class JsonSchemaDocumentSnapshot implements DocumentSnapshot, Persistable<JsonSchemaDocumentSnapshotId> {

    @EmbeddedId
    private JsonSchemaDocumentSnapshotId id;

    @Column(name = "json_schema_document_snapshot_created_on", columnDefinition = "DATETIME", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "json_schema_document_snapshot_created_by", columnDefinition = "VARCHAR(255)")
    private String createdBy;

    @Embedded
    private HistoricDocument document;

    @Transient
    private transient boolean isNew = false;

    public JsonSchemaDocumentSnapshot(
        JsonSchemaDocument document,
        LocalDateTime createdOn,
        String createdBy,
        JsonSchemaDocumentDefinition documentDefinition
    ) {
        this.id = JsonSchemaDocumentSnapshotId.newId(UUID.randomUUID());
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.document = new HistoricDocument(document, documentDefinition);
        this.isNew = true;
    }

    @Override
    public String id() {
        return id.toString();
    }

    @Override
    public LocalDateTime snapshotCreatedOn() {
        return createdOn;
    }

    @Override
    public String snapshotCreatedBy() {
        return createdBy;
    }

    @Override
    public HistoricDocument document() {
        return document;
    }

    @Override
    public JsonSchemaDocumentSnapshotId getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}