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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "json_schema_document_definition_role")
public class JsonSchemaDocumentDefinitionRole implements DocumentDefinitionRole {

    @EmbeddedId
    private JsonSchemaDocumentDefinitionRoleId id;

    public JsonSchemaDocumentDefinitionRole(JsonSchemaDocumentDefinitionRoleId id) {
        this.id = id;
    }

    JsonSchemaDocumentDefinitionRole() {
    }

    @Override
    public Id id() {
        return id;
    }
}