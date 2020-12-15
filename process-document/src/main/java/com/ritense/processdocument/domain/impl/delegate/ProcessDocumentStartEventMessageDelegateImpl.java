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

package com.ritense.processdocument.domain.impl.delegate;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.relation.DocumentRelationType;
import com.ritense.document.service.DocumentService;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.delegate.ProcessDocumentStartEventMessageDelegate;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.valtimo.contract.json.JsonPointerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Slf4j
@RequiredArgsConstructor
public class ProcessDocumentStartEventMessageDelegateImpl implements ProcessDocumentStartEventMessageDelegate {

    public static final String SOURCE_PROCESS_INSTANCE_ID = "sourceProcessInstanceId";
    public static final String RELATION_TYPE = "relationType";
    public static final String PAYLOAD = "payload";

    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final DocumentService documentService;
    private final RuntimeService runtimeService;

    public void deliver(DelegateExecution execution, String message) {
        deliver(execution, message, DocumentRelationType.PREVIOUS.name());
    }

    public void deliver(DelegateExecution execution, String message, String relationType) {
        assertArgumentNotNull(message, "message is required");
        assertArgumentNotNull(relationType, "relationType is required");

        final var processInstanceId = ProcessInstanceId.fromExecution(execution, CamundaProcessInstanceId.class);
        processDocumentAssociationService.findProcessDocumentInstance(processInstanceId)
            .flatMap(processDocumentInstance -> documentService.findBy(processDocumentInstance.processDocumentInstanceId().documentId()))
            .ifPresent(document -> {
                final JsonNode payload = getPayload(execution, document);
                runtimeService.createMessageCorrelation(message)
                    .processInstanceBusinessKey(document.id().toString())
                    .setVariable(SOURCE_PROCESS_INSTANCE_ID, execution.getProcessInstanceId())
                    .setVariable(RELATION_TYPE, DocumentRelationType.valueOf(relationType))
                    .setVariable(PAYLOAD, payload.toString())
                    .correlateStartMessage();
            });
    }

    private JsonNode getPayload(DelegateExecution execution, Document document) {
        final ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        execution.getVariables().forEach((key, value) -> {
            try {
                final JsonPointer targetJsonPointer = JsonPointer.valueOf(key);
                final JsonPointer sourceJsonPointer = JsonPointer.valueOf(String.valueOf(value));
                document.content().getValueBy(sourceJsonPointer)
                    .ifPresent(sourceValue -> JsonPointerHelper.appendJsonPointerTo(rootNode, targetJsonPointer, sourceValue));
            } catch (IllegalArgumentException e) {
                //Skip key/value
                logger.info("Skipping var: cannot parse key/value to jsonPointer {} - {}", key, value);
            }
        });
        logger.info("Parsed all process variables to json string: {}", rootNode.toString());
        return rootNode;
    }

}