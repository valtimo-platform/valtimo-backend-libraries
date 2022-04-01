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

package com.ritense.document.domain.impl;

import com.ritense.document.domain.DocumentDefinitionRole;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class JsonSchemaDocumentDefinitionRoleId implements DocumentDefinitionRole.Id, Serializable {

    @Column(name = "document_definition_name", length = 50, columnDefinition = "VARCHAR(50)", nullable = false, updatable = false)
    private String documentDefinitionName;

    @Column(name = "role", columnDefinition = "VARCHAR(50)", nullable = false, updatable = false)
    private String role;

    public JsonSchemaDocumentDefinitionRoleId(String documentDefinitionName, String role) {
        this.documentDefinitionName = documentDefinitionName;
        this.role = role;
    }

    private JsonSchemaDocumentDefinitionRoleId() {
    }

    public String documentDefinitionName() {
        return documentDefinitionName;
    }

    public String role() {
        return role;
    }

    @Override
    public String toString() {
        return documentDefinitionName + ":" + role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonSchemaDocumentDefinitionRoleId that = (JsonSchemaDocumentDefinitionRoleId) o;
        return Objects.equals(documentDefinitionName, that.documentDefinitionName) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentDefinitionName, role);
    }
}