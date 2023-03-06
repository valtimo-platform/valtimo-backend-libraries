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
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.event.DocumentFieldChangedEvent;

public class JsonSchemaDocumentFieldChangedEvent implements DocumentFieldChangedEvent {

    private final String type;
    private final String path;
    private final JsonNode value;
    private final JsonNode fromValue;

    @JsonCreator
    public JsonSchemaDocumentFieldChangedEvent(
        String type,
        String path,
        JsonNode value,
        JsonNode fromValue
    ) {
        this.type = type;
        this.path = path;
        this.value = value;
        this.fromValue = fromValue;
    }

    public static JsonSchemaDocumentFieldChangedEvent fromJsonNode(JsonNode jsonNode) {
        return new JsonSchemaDocumentFieldChangedEvent(
            jsonNode.get("op").textValue(),
            jsonNode.get("path").textValue(),
            jsonNode.get("value"),
            jsonNode.get("fromValue")
        );
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public JsonNode value() {
        return value;
    }

    @Override
    public JsonNode fromValue() {
        return fromValue;
    }

}