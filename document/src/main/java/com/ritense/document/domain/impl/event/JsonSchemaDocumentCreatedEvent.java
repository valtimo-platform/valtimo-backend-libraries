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

package com.ritense.document.domain.impl.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.document.domain.event.DocumentCreatedEvent;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.JsonSchemaDocumentVersion;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;

import com.ritense.valtimo.contract.audit.view.AuditView;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class JsonSchemaDocumentCreatedEvent extends AuditMetaData implements DocumentCreatedEvent, AuditEvent {

    private final JsonSchemaDocumentId documentId;
    private final JsonSchemaDocumentDefinitionId definitionId;
    private final JsonSchemaDocumentVersion version;

    @JsonCreator
    public JsonSchemaDocumentCreatedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        JsonSchemaDocumentId documentId,
        JsonSchemaDocumentDefinitionId definitionId,
        JsonSchemaDocumentVersion version
    ) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(documentId, "documentId is required");
        assertArgumentNotNull(definitionId, "definitionId is required");
        assertArgumentNotNull(version, "version is required");
        this.documentId = documentId;
        this.definitionId = definitionId;
        this.version = version;
    }

    @Override
    public JsonSchemaDocumentId documentId() {
        return documentId;
    }

    @Override
    public JsonSchemaDocumentDefinitionId definitionId() {
        return definitionId;
    }

    @Override
    public JsonSchemaDocumentVersion version() {
        return version;
    }

    @Override
    @JsonView(AuditView.Internal.class)
    @JsonIgnore(false)
    public UUID getDocumentId() {
        return documentId.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonSchemaDocumentCreatedEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        JsonSchemaDocumentCreatedEvent that = (JsonSchemaDocumentCreatedEvent) o;
        return documentId.equals(that.documentId) &&
            definitionId.equals(that.definitionId) &&
            version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), documentId, definitionId, version);
    }

}