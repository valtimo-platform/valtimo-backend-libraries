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

package com.ritense.processdocument.domain.impl.listener;

import static com.ritense.processdocument.domain.impl.delegate.ProcessDocumentStartEventMessageDelegateImpl.PAYLOAD;
import static com.ritense.processdocument.domain.impl.delegate.ProcessDocumentStartEventMessageDelegateImpl.RELATION_TYPE;
import static com.ritense.processdocument.domain.impl.delegate.ProcessDocumentStartEventMessageDelegateImpl.SOURCE_PROCESS_INSTANCE_ID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.domain.impl.request.DocumentRelationRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.relation.DocumentRelationType;
import com.ritense.processdocument.domain.ProcessDefinitionKey;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.impl.event.NextJsonSchemaDocumentRelationAvailableEvent;
import com.ritense.processdocument.domain.impl.request.NewDocumentForRunningProcessRequest;
import com.ritense.processdocument.domain.listener.StartEventListener;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import java.io.IOException;
import java.util.UUID;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

@CamundaSelector(type = ActivityTypes.START_EVENT, event = ExecutionListener.EVENTNAME_START)
public class StartEventListenerImpl extends ReactorExecutionListener implements StartEventListener {

    private static final Logger logger = LoggerFactory.getLogger(StartEventListenerImpl.class);
    private final ProcessDocumentService processDocumentService;
    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    public StartEventListenerImpl(
        ProcessDocumentService processDocumentService,
        ProcessDocumentAssociationService processDocumentAssociationService,
        ApplicationEventPublisher applicationEventPublisher,
        ObjectMapper objectMapper
    ) {
        this.processDocumentService = processDocumentService;
        this.processDocumentAssociationService = processDocumentAssociationService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notify(DelegateExecution execution) {
        if (execution.hasVariable(SOURCE_PROCESS_INSTANCE_ID)) {
            logger.info("Start event listener with source relation");
            final var sourceProcessInstanceId = new CamundaProcessInstanceId(getStringValue(execution, SOURCE_PROCESS_INSTANCE_ID));
            final var documentRelationType = (DocumentRelationType) execution.getVariable(RELATION_TYPE);
            final var processDefinitionKey = ProcessDefinitionKey.fromExecution(execution, CamundaProcessDefinitionKey.class);
            final var processInstanceId = ProcessInstanceId.fromExecution(execution, CamundaProcessInstanceId.class);

            AuthorizationContext.runWithoutAuthorization(() -> {
                final var documentDefinitionId = processDocumentAssociationService.findProcessDocumentDefinition(
                        processDefinitionKey)
                    .map(definition -> definition.processDocumentDefinitionId().documentDefinitionId())
                    .orElse(null);

                if (documentDefinitionId != null) {
                    final var jsonData = extractJsonDocumentData(execution);

                    final var sourceDocumentId = processDocumentAssociationService.findProcessDocumentInstance(
                            sourceProcessInstanceId)
                        .map(instance -> instance.processDocumentInstanceId().documentId())
                        .orElse(null);

                    if (sourceDocumentId != null) {
                        var newDocumentRequest = new NewDocumentRequest(
                            documentDefinitionId.name(),
                            jsonData
                        ).withDocumentRelation(new DocumentRelationRequest(UUID.fromString(sourceDocumentId.toString()),
                            documentRelationType
                        ));

                        final var request = new NewDocumentForRunningProcessRequest(
                            processDefinitionKey.toString(),
                            processInstanceId.toString(),
                            newDocumentRequest
                        );
                        final var result = processDocumentService.newDocumentForRunningProcess(request);

                        result.resultingDocument().ifPresentOrElse(document -> applicationEventPublisher.publishEvent(
                                new NextJsonSchemaDocumentRelationAvailableEvent(
                                    sourceDocumentId.toString(),
                                    document.id().toString()
                                )
                            ), () -> {
                                throw new RuntimeException(String.format(
                                    "Unable to create new document %s for process %s",
                                    documentDefinitionId,
                                    processDefinitionKey
                                ));
                            }
                        );
                    }
                }
                return null;
            });
        }
    }

    private JsonNode extractJsonDocumentData(DelegateExecution execution) {
        final String rawJsonData = (String) execution.getVariable(PAYLOAD);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(rawJsonData);
        } catch (IOException e) {
            throw new RuntimeException("extractJsonDocumentData failed");
        }
        return jsonData;
    }

    private String getStringValue(DelegateExecution execution, String key) {
        final StringValue variableTyped = execution.getVariableTyped(key);
        return variableTyped.getValue();
    }

}