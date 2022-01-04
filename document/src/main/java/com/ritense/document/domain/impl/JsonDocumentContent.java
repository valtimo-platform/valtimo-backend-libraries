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

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.DocumentContent;
import com.ritense.document.domain.diff.JsonDifferenceService;
import com.ritense.document.domain.patch.JsonPatchService;
import com.ritense.valtimo.contract.json.patch.JsonPatch;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.Optional;
import org.hibernate.annotations.Type;

import static com.ritense.document.domain.patch.JsonPatchFilterFlag.allowArrayRemovalOperations;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonDocumentContent implements DocumentContent {

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "json_document_content", columnDefinition = "json")
    private String content;

    public static JsonDocumentContent build(JsonNode currentContent, JsonNode modifiedContent) {
        assertArgumentNotNull(currentContent, "currentContent is required");
        assertArgumentNotNull(modifiedContent, "modifiedContent is required");

        //Diff patching to support partial changes
        final var patchDiff = JsonDifferenceService.diff(currentContent, modifiedContent);
        JsonPatchService.apply(patchDiff, currentContent, allowArrayRemovalOperations());
        return JsonDocumentContent.build(currentContent);
    }

    public static JsonDocumentContent build(JsonNode currentContent, JsonNode modifiedContent, JsonPatch prePatch) {
        //Pre patching
        if (prePatch != null && !prePatch.patches().isEmpty()) {
            JsonPatchService.apply(prePatch, currentContent);
        }
        return build(currentContent, modifiedContent);
    }

    public static JsonDocumentContent build(JsonNode jsonNode) {
        return new JsonDocumentContent(jsonNode.toString());
    }

    public JsonDocumentContent(final String jsonString) {
        assertArgumentNotEmpty(jsonString, "jsonString is required");
        this.content = jsonString;
    }

    public JsonDocumentContent(JsonDocumentContent another) {
        this.content = another.content;
    }

    @Override
    public JsonNode diff(DocumentContent another) {
        return JsonDifferenceService.diff(asJson(), another.asJson());
    }

    @Override
    public Optional<JsonNode> getValueBy(final JsonPointer jsonPointer) {
        final JsonNode jsonNode = asJson().at(jsonPointer);
        if (jsonNode.isMissingNode() || jsonNode.isNull()) {
            return Optional.empty();
        }
        return Optional.of(jsonNode);
    }

    @Override
    public JsonNode asJson() {
        try {
            return Mapper.INSTANCE.get().readTree(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonDocumentContent)) {
            return false;
        }
        JsonDocumentContent that = (JsonDocumentContent) o;
        return content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

}