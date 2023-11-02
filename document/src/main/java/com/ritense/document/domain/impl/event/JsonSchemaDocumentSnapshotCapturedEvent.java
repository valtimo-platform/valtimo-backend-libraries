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

import com.ritense.document.domain.event.DocumentSnapshotCapturedEvent;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import java.time.LocalDateTime;

public class JsonSchemaDocumentSnapshotCapturedEvent implements DocumentSnapshotCapturedEvent {

    private final JsonSchemaDocumentId documentId;
    private final LocalDateTime createdOn;
    private final String createdBy;
    private final String tenantId;

    public JsonSchemaDocumentSnapshotCapturedEvent(
        final JsonSchemaDocumentId documentId,
        final LocalDateTime createdOn,
        final String createdBy,
        final String tenantId
    ) {
        this.documentId = documentId;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.tenantId = tenantId;
    }

    @Override
    public JsonSchemaDocumentId documentId() {
        return documentId;
    }

    @Override
    public LocalDateTime createdOn() {
        return createdOn;
    }

    @Override
    public String createdBy() {
        return createdBy;
    }

    public String tenantId() {
        return tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonSchemaDocumentSnapshotCapturedEvent that = (JsonSchemaDocumentSnapshotCapturedEvent) o;

        if (!documentId.equals(that.documentId)) return false;
        if (!createdOn.equals(that.createdOn)) return false;
        if (!createdBy.equals(that.createdBy)) return false;
        return tenantId.equals(that.tenantId);
    }

    @Override
    public int hashCode() {
        int result = documentId.hashCode();
        result = 31 * result + createdOn.hashCode();
        result = 31 * result + createdBy.hashCode();
        result = 31 * result + tenantId.hashCode();
        return result;
    }
}