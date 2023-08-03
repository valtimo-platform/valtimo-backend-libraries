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

package com.ritense.document.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import com.ritense.valtimo.contract.audit.view.AuditView;
import com.ritense.valtimo.contract.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class DocumentAssigneeChangedEvent extends AuditMetaData
    implements AuditEvent, DomainEvent {

    private String assigneeName;
    private UUID documentId;

    @JsonCreator
    public DocumentAssigneeChangedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        UUID documentId,
        String assigneeName
    ) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(documentId, "documentId is required");
        assertArgumentNotNull(assigneeName, "assignee name is required");
        this.documentId = documentId;
        this.assigneeName = assigneeName;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    @Override
    @JsonView(AuditView.Internal.class)
    @JsonIgnore(false)
    public UUID getDocumentId() {
        return documentId;
    }


    @JsonProperty
    @JsonView(AuditView.Public.class)
    public String getAssigneeName() {
        return assigneeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentAssigneeChangedEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DocumentAssigneeChangedEvent that = (DocumentAssigneeChangedEvent) o;
        return Objects.equals(assigneeName, that.assigneeName)
            && Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), assigneeName, documentId);
    }
}
