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

package com.ritense.processdocument.domain.impl.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.impl.Mapper;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.UUID;

import static com.ritense.processdocument.domain.impl.delegate.ProcessDocumentStartEventMessageDelegateImpl.PAYLOAD;
import static com.ritense.processdocument.domain.impl.delegate.ProcessDocumentStartEventMessageDelegateImpl.RELATION_TYPE;
import static com.ritense.processdocument.domain.impl.delegate.ProcessDocumentStartEventMessageDelegateImpl.SOURCE_PROCESS_INSTANCE_ID;

@Slf4j
@RequiredArgsConstructor
@CamundaSelector(type = ActivityTypes.START_EVENT, event = ExecutionListener.EVENTNAME_START)
public class StartEventListenerImpl extends ReactorExecutionListener implements StartEventListener {

    private final ProcessDocumentService processDocumentService;
    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void notify(DelegateExecution execution) {
        if (execution.hasVariable(SOURCE_PROCESS_INSTANCE_ID)) {
            logger.info("Start event listener with source relation");
            final var sourceProcessInstanceId = new CamundaProcessInstanceId(getStringValue(execution, SOURCE_PROCESS_INSTANCE_ID));
            final var documentRelationType = (DocumentRelationType) execution.getVariable(RELATION_TYPE);
            final var processDefinitionKey = ProcessDefinitionKey.fromExecution(execution, CamundaProcessDefinitionKey.class);
            final var processInstanceId = ProcessInstanceId.fromExecution(execution, CamundaProcessInstanceId.class);

            processDocumentAssociationService.findProcessDocumentDefinition(processDefinitionKey).ifPresent(processDocumentDefinition -> {

                final var documentDefinitionId = processDocumentDefinition.processDocumentDefinitionId().documentDefinitionId();
                final var jsonData = extractJsonDocumentData(execution);

                processDocumentAssociationService.findProcessDocumentInstance(sourceProcessInstanceId)
                    .ifPresent(sourceProcessDocumentInstance -> {
                        final var sourceDocumentId = sourceProcessDocumentInstance.processDocumentInstanceId().documentId();

                        var newDocumentRequest = new NewDocumentRequest(
                            documentDefinitionId.name(),
                            jsonData
                        ).withDocumentRelation(new DocumentRelationRequest(UUID.fromString(sourceDocumentId.toString()), documentRelationType));

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
                    });
            });
        }
    }

    private JsonNode extractJsonDocumentData(DelegateExecution execution) {
        final String rawJsonData = (String) execution.getVariable(PAYLOAD);
        JsonNode jsonData;
        try {
            jsonData = Mapper.INSTANCE.get().readTree(rawJsonData);
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