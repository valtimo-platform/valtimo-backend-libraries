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

package com.ritense.document.service.impl;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ritense.document.domain.Document;
import com.ritense.document.service.DocumentVariableService;

public class JsonSchemaDocumentVariableService implements DocumentVariableService {
    @Override
    public String getTextOrReturnEmptyString(Document document, String path) {
        return document.content().getValueBy(JsonPointer.valueOf(path)).orElse(TextNode.valueOf("")).asText();
    }

    @Override
    public String getTextOrThrow(Document document, String path) {
        return document.content().getValueBy(JsonPointer.valueOf(path)).orElseThrow().asText();
    }

    @Override
    public JsonNode getNodeOrThrow(Document document, String path) {
        return document.content().getValueBy(JsonPointer.valueOf(path)).orElseThrow();
    }

    @Override
    public Integer getIntegerOrReturnZero(Document document, String path) {
        return document.content().getValueBy(JsonPointer.valueOf(path)).orElse(IntNode.valueOf(0)).asInt();
    }

    @Override
    public Boolean getBooleanOrReturnFalse(Document document, String path) {
        return document.content().getValueBy(JsonPointer.valueOf(path)).orElse(BooleanNode.valueOf(false)).asBoolean();
    }
}
