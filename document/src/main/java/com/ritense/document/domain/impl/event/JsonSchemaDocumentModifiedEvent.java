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

package com.ritense.document.domain.impl.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.document.domain.event.DocumentModifiedEvent;
import com.ritense.document.domain.impl.JsonSchemaDocumentFieldChangedEvent;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import com.ritense.valtimo.contract.audit.view.AuditView;
import com.ritense.valtimo.contract.domain.DomainEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class JsonSchemaDocumentModifiedEvent
    extends AuditMetaData implements DocumentModifiedEvent, AuditEvent, DomainEvent {

    private final JsonSchemaDocumentId documentId;
    private final List<JsonSchemaDocumentFieldChangedEvent> changes;
    private final String tenantId;

    @JsonCreator
    public JsonSchemaDocumentModifiedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        JsonSchemaDocumentId documentId,
        List<JsonSchemaDocumentFieldChangedEvent> changes,
        String tenantId
    ) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(documentId, "documentId is required");
        this.documentId = documentId;
        this.changes = changes;
        this.tenantId = tenantId;
    }

    @Override
    public JsonSchemaDocumentId documentId() {
        return documentId;
    }

    @Override
    public List<JsonSchemaDocumentFieldChangedEvent> changes() {
        return changes;
    }

    @Override
    @JsonView(AuditView.Internal.class)
    @JsonIgnore(false)
    public UUID getDocumentId() {
        return documentId.getId();
    }

    public String tenantId() {
        return tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        JsonSchemaDocumentModifiedEvent that = (JsonSchemaDocumentModifiedEvent) o;

        if (!documentId.equals(that.documentId)) return false;
        if (!changes.equals(that.changes)) return false;
        return tenantId.equals(that.tenantId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + documentId.hashCode();
        result = 31 * result + changes.hashCode();
        result = 31 * result + tenantId.hashCode();
        return result;
    }
}