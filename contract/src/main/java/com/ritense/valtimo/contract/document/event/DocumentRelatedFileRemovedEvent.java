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

package com.ritense.valtimo.contract.document.event;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import com.ritense.valtimo.contract.audit.view.AuditView;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class DocumentRelatedFileRemovedEvent extends AuditMetaData implements AuditEvent {

    private final UUID documentId;
    private final UUID fileId;
    private final String fileName;

    @JsonCreator
    public DocumentRelatedFileRemovedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        UUID documentId,
        UUID fileId,
        String fileName
    ) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(documentId, "documentId is required");
        assertArgumentNotNull(fileId, "fileId is required");
        assertArgumentNotNull(fileName, "fileName is required");
        this.documentId = documentId;
        this.fileId = fileId;
        this.fileName = fileName;
    }

    @Override
    @JsonView(AuditView.Internal.class)
    @JsonIgnore(false)
    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DocumentRelatedFileRemovedEvent that = (DocumentRelatedFileRemovedEvent) o;
        return Objects.equals(documentId, that.documentId) && Objects.equals(fileId, that.fileId) && Objects.equals(
            fileName,
            that.fileName
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), documentId, fileId, fileName);
    }
}