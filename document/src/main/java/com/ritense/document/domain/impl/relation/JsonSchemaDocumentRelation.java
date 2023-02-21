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

package com.ritense.document.domain.impl.relation;

import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.request.DocumentRelationRequest;
import com.ritense.document.domain.relation.DocumentRelation;
import com.ritense.document.domain.relation.DocumentRelationType;
import java.io.Serializable;
import java.util.Objects;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class JsonSchemaDocumentRelation implements DocumentRelation, Serializable {

    private static final long serialVersionUID = 4766534733755676635L;

    private JsonSchemaDocumentId id;
    private DocumentRelationType relationType;

    private JsonSchemaDocumentRelation() {
    }

    public JsonSchemaDocumentRelation(
        final JsonSchemaDocumentId id,
        final DocumentRelationType relationType
    ) {
        assertArgumentNotNull(id, "id is required");
        assertArgumentNotNull(relationType, "relationType is required");
        this.id = id;
        this.relationType = relationType;
    }

    public static JsonSchemaDocumentRelation from(DocumentRelationRequest documentRelation) {
        if (documentRelation != null) {
            return new JsonSchemaDocumentRelation(
                JsonSchemaDocumentId.existingId(documentRelation.getDocumentId()),
                documentRelation.getRelationType()
            );
        }
        return null;
    }

    @Override
    public JsonSchemaDocumentId id() {
        return id;
    }

    @Override
    public DocumentRelationType relationType() {
        return relationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonSchemaDocumentRelation)) {
            return false;
        }
        JsonSchemaDocumentRelation that = (JsonSchemaDocumentRelation) o;
        return id.equals(that.id) &&
            relationType == that.relationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, relationType);
    }
}