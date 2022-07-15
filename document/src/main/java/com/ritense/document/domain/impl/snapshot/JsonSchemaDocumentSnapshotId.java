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

import com.ritense.document.domain.snapshot.DocumentSnapshot;
import com.ritense.valtimo.contract.domain.AbstractId;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class JsonSchemaDocumentSnapshotId extends AbstractId<JsonSchemaDocumentSnapshotId> implements DocumentSnapshot.Id {

    @Column(name = "json_schema_document_snapshot_id", unique = true, nullable = false, updatable = false)
    private UUID id;

    private JsonSchemaDocumentSnapshotId(@NotNull UUID id) {
        if (id == null) {
            throw new NullPointerException(("id is marked non-null but is null"));
        }
        this.id = id;
    }

    private JsonSchemaDocumentSnapshotId() {
    }

    public static JsonSchemaDocumentSnapshotId existingId(UUID id) {
        return new JsonSchemaDocumentSnapshotId(id);
    }

    public static JsonSchemaDocumentSnapshotId newId(UUID id) {
        return new JsonSchemaDocumentSnapshotId(id).newIdentity();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonSchemaDocumentSnapshotId that = (JsonSchemaDocumentSnapshotId) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}