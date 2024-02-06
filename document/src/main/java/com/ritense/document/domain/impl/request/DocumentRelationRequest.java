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

package com.ritense.document.domain.impl.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.document.domain.relation.DocumentRelationType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class DocumentRelationRequest {

    @JsonProperty("id")
    private final UUID documentId;

    @JsonProperty
    private final DocumentRelationType relationType;

    @JsonCreator
    public DocumentRelationRequest(
        @JsonProperty(value = "id", required = true) @NotNull UUID documentId,
        @JsonProperty(value = "relationType", required = true) @NotNull DocumentRelationType relationType
    ) {
        this.documentId = documentId;
        this.relationType = relationType;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public DocumentRelationType getRelationType() {
        return relationType;
    }

}