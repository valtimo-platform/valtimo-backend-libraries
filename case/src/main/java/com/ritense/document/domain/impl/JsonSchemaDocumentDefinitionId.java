/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentTrue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import com.ritense.valtimo.contract.domain.AbstractId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.util.Objects;

@Embeddable
public class JsonSchemaDocumentDefinitionId extends AbstractId<JsonSchemaDocumentDefinitionId>
    implements DocumentDefinition.Id {

    @Column(name = "document_definition_name", length = 50, columnDefinition = "VARCHAR(50)", nullable = false, updatable = true)
    private String name;

    @Embedded
    private CaseDefinitionId caseDefinitionId;

    @JsonCreator
    private JsonSchemaDocumentDefinitionId(String name, CaseDefinitionId caseDefinitionId) {
        assertArgumentId(name, caseDefinitionId);
        this.name = name;
        this.caseDefinitionId = caseDefinitionId;
    }

    private JsonSchemaDocumentDefinitionId() {
    }

    private void assertArgumentId(String name, CaseDefinitionId caseDefinitionId) {
        assertArgumentNotNull(name, "name is required");
        assertArgumentLength(name, 1, 50, "name must be between 1-50 characters");
        assertArgumentTrue(name.matches("[A-z0-9-_.]+"), "name contains illegal character. For name: " + name);
        assertArgumentNotNull(caseDefinitionId, "CaseDefinitionId is required");
    }

    /*
     * @Deprecated This method is deprecated and should not be used. Use `of(String, CaseDefinitionId)` instead.
     */
    @Deprecated()
    public static JsonSchemaDocumentDefinitionId existingId(String name, CaseDefinitionId caseDefinitionId) {
        return new JsonSchemaDocumentDefinitionId(name, caseDefinitionId);
    }

    public static JsonSchemaDocumentDefinitionId of(String name, CaseDefinitionId caseDefinitionId) {
        return new JsonSchemaDocumentDefinitionId(name, caseDefinitionId);
    }

    public static JsonSchemaDocumentDefinitionId existingId(DocumentDefinition.Id documentDefinitionId) {
        return (JsonSchemaDocumentDefinitionId) documentDefinitionId;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CaseDefinitionId caseDefinitionId() {
        return caseDefinitionId;
    }

    @Override
    public String toString() {
        return name + ":" + caseDefinitionId.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JsonSchemaDocumentDefinitionId that = (JsonSchemaDocumentDefinitionId) o;
        return caseDefinitionId.equals(that.caseDefinitionId) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, caseDefinitionId);
    }
}