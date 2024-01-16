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

package com.ritense.document.domain.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.document.domain.Document;
import com.ritense.valtimo.contract.domain.AbstractId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Embeddable
public class JsonSchemaDocumentId extends AbstractId<JsonSchemaDocumentId> implements Document.Id {

    @Column(name = "json_schema_document_id", updatable = false)
    private UUID id;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    private JsonSchemaDocumentId(UUID id) {
        assertArgumentNotNull(id, "id is required");
        this.id = id;
    }

    JsonSchemaDocumentId() {
    }

    public static JsonSchemaDocumentId existingId(UUID id) {
        return new JsonSchemaDocumentId(id);
    }

    public static JsonSchemaDocumentId newId(UUID id) {
        return new JsonSchemaDocumentId(id).newIdentity();
    }

    @Override
    @JsonProperty
    public String toString() {
        return id.toString();
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonSchemaDocumentId that = (JsonSchemaDocumentId) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}