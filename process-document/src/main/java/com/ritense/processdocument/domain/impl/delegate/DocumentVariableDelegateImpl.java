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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.POJONode;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.DocumentService;
import com.ritense.processdocument.domain.delegate.DocumentVariableDelegate;
import com.ritense.valtimo.contract.json.Mapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DocumentVariableDelegateImpl implements DocumentVariableDelegate {

    private static final Logger logger = LoggerFactory.getLogger(DocumentVariableDelegateImpl.class);
    private static final ObjectMapper mapper = Mapper.INSTANCE.get().copy()
        .disable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .disable(DeserializationFeature.USE_LONG_FOR_INTS)
        .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
    private final DocumentService documentService;

    public DocumentVariableDelegateImpl(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public Object findValueByJsonPointer(String jsonPointer, DelegateExecution execution) {
        return Optional.ofNullable(findValueByJsonPointerOrDefault(jsonPointer, execution, null))
            .orElseThrow();
    }

    @Override
    public Object findValueByJsonPointerOrDefault(String jsonPointer, DelegateExecution execution, Object defaultValue) {
        final var jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(execution.getProcessBusinessKey()));
        logger.debug("Retrieving value for key {} from documentId {}", jsonPointer, execution.getProcessBusinessKey());
        return documentService
            .findBy(jsonSchemaDocumentId)
            .flatMap(jsonSchemaDocument -> jsonSchemaDocument.content().getValueBy(JsonPointer.valueOf(jsonPointer)))
            .map(this::transform)
            .orElse(defaultValue);
    }

    private Object transform(JsonNode jsonNode) {
        if(jsonNode.isNumber()) {
            // Removing this would result in a breaking change, as 3.0 will become an int when using treeToValue
            return jsonNode.asDouble();
        } else if(jsonNode.isValueNode() || jsonNode.isContainerNode()) {
            try {
                return mapper.treeToValue(jsonNode, Object.class);
            } catch (JsonProcessingException e) {
                logger.error("Could not transform JsonNode of type \"" + jsonNode.getNodeType() + "\"", e);
            }
        } else {
            logger.debug(
                "JsonNode of type \"" + jsonNode.getNodeType() + "\" cannot be transformed to a value. Returning null.");
        }
        return null;
    }

}