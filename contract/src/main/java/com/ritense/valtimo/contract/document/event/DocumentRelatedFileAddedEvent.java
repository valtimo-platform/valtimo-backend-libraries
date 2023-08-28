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

package com.ritense.valtimo.contract.document.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import com.ritense.valtimo.contract.audit.view.AuditView;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class DocumentRelatedFileAddedEvent extends AuditMetaData implements AuditEvent {

    private final UUID documentId;
    private final UUID fileId;
    private final String fileName;

    private final Map<String, Object> metadata;

    @JsonCreator
    public DocumentRelatedFileAddedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        UUID documentId,
        UUID fileId,
        String fileName,
        Map<String, Object> metadata
    ) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(documentId, "documentId is required");
        assertArgumentNotNull(fileId, "fileId is required");
        assertArgumentNotNull(fileName, "fileName is required");
        this.documentId = documentId;
        this.fileId = fileId;
        this.fileName = fileName;
        this.metadata = metadata;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}