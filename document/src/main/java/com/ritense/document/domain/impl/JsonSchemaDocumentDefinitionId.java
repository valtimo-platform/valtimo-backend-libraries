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
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.valtimo.contract.domain.AbstractId;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.util.Objects;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentRange;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentTrue;

@Embeddable
public class JsonSchemaDocumentDefinitionId extends AbstractId<JsonSchemaDocumentDefinitionId>
    implements DocumentDefinition.Id {

    @Transient
    private static final long INITIAL_VERSION = 1;

    @Column(name = "document_definition_name", length = 50, columnDefinition = "VARCHAR(50)", nullable = false, updatable = false)
    private String name;

    @Column(name = "document_definition_version", columnDefinition = "BIGINT", nullable = false, updatable = false)
    private long version;

    private JsonSchemaDocumentDefinitionId(String id) {
        assertArgumentNotNull(id, "id is required");
        assertArgumentTrue(id.contains(":"), "id doesn't contain split operator ':'. For id: " + id);
        String[] idParts = id.split(":");
        this.name = idParts[0];
        this.version = Long.parseLong(idParts[1]);
        assertArgumentId(name, version);
    }

    @JsonCreator
    private JsonSchemaDocumentDefinitionId(String name, long version) {
        assertArgumentId(name, version);
        this.name = name;
        this.version = version;
    }

    private JsonSchemaDocumentDefinitionId() {
    }

    private void assertArgumentId(String name, long version) {
        assertArgumentNotNull(name, "name is required");
        assertArgumentLength(name, 5, 50, "name must be between 5-50 characters");
        assertArgumentTrue(name.matches("[A-z0-9-_.]+"), "name contains illegal character. For name: " + name);
        assertArgumentRange(version, INITIAL_VERSION, Long.MAX_VALUE, "version should be >= 1. For version: " + version);
    }

    public static JsonSchemaDocumentDefinitionId nextVersion(DocumentDefinition.Id documentDefinitionId) {
        return new JsonSchemaDocumentDefinitionId(documentDefinitionId.name(), documentDefinitionId.version() + 1).newIdentity();
    }

    public static JsonSchemaDocumentDefinitionId existingId(DocumentDefinition.Id documentDefinitionId) {
        return (JsonSchemaDocumentDefinitionId) documentDefinitionId;
    }

    public static JsonSchemaDocumentDefinitionId existingId(String name, long version) {
        return new JsonSchemaDocumentDefinitionId(name, version);
    }

    public static JsonSchemaDocumentDefinitionId newId(String name) {
        return new JsonSchemaDocumentDefinitionId(name, INITIAL_VERSION).newIdentity();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long version() {
        return version;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonSchemaDocumentDefinitionId that = (JsonSchemaDocumentDefinitionId) o;
        return version == that.version && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }
}