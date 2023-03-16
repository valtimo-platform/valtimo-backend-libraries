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

package com.ritense.processdocument.domain.impl.delegate;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.DocumentService;
import com.ritense.processdocument.domain.delegate.DocumentVariableDelegate;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DocumentVariableDelegateImpl implements DocumentVariableDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DocumentVariableDelegateImpl.class);
    private final DocumentService documentService;

    public DocumentVariableDelegateImpl(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public Object findValueByJsonPointer(String jsonPointer, DelegateExecution execution) {
        final var jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(execution.getProcessBusinessKey()));
        logger.debug("Retrieving value for key {} from documentId {}", jsonPointer, execution.getProcessBusinessKey());
        return documentService
            .findBy(jsonSchemaDocumentId)
            .map(jsonSchemaDocument -> transform(jsonSchemaDocument.content().getValueBy(JsonPointer.valueOf(jsonPointer)).orElseThrow()))
            .orElseThrow();
    }

    @Override
    public Object findValueByJsonPointerOrDefault(String jsonPointer, DelegateExecution execution, Object defaultValue) {
        final var jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(execution.getProcessBusinessKey()));
        logger.debug("Retrieving value for key {} from documentId {}", jsonPointer, execution.getProcessBusinessKey());
        return documentService
            .findBy(jsonSchemaDocumentId)
            .map(jsonSchemaDocument -> transform(jsonSchemaDocument.content().getValueBy(JsonPointer.valueOf(jsonPointer)).orElseThrow()))
            .orElse(defaultValue);
    }

    private Object transform(JsonNode jsonNode) {
        switch (jsonNode.getNodeType()) {
            case STRING:
                return jsonNode.asText();
            case NUMBER:
                return jsonNode.asDouble();
            case BOOLEAN:
                return jsonNode.asBoolean();
            case ARRAY:
                List<Object> collection = new ArrayList<>();
                jsonNode.elements().forEachRemaining(collection::add);
            return collection;
            default:
                throw new IllegalStateException("JsonNode of type \"" + jsonNode.getNodeType() + "\" cannot be converted to a value");
        }
    }

}