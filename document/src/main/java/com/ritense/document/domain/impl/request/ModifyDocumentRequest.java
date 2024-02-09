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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.Document;
import com.ritense.valtimo.contract.json.patch.JsonPatch;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class ModifyDocumentRequest {

    @JsonProperty
    @NotNull
    private final String documentId;

    @JsonProperty
    @NotNull
    private final JsonNode content;

    @JsonIgnore
    private JsonPatch jsonPatch;

    @JsonCreator
    public ModifyDocumentRequest(
        @JsonProperty(value = "documentId", required = true) String documentId,
        @JsonProperty(value = "content", required = true) JsonNode content
    ) {
        this.documentId = documentId;
        this.content = content;
    }

    public static ModifyDocumentRequest create(Document document, JsonNode jsonNode) {
        return new ModifyDocumentRequest(
            document.id().toString(),
            jsonNode
        );
    }

    public String documentId() {
        return documentId;
    }

    public JsonNode content() {
        return content;
    }

    public ModifyDocumentRequest withJsonPatch(JsonPatch jsonPatch) {
        this.jsonPatch = jsonPatch;
        return this;
    }

    public JsonPatch jsonPatch() {
        return jsonPatch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModifyDocumentRequest request)) {
            return false;
        }
        return documentId.equals(request.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }
}