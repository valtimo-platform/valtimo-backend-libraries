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

import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.listener.StartEventFromCallActivityListener;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener;
import org.camunda.bpm.model.bpmn.impl.instance.ProcessImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

@CamundaSelector(type = ActivityTypes.START_EVENT, event = ExecutionListener.EVENTNAME_START)
public class StartEventFromCallActivityListenerImpl extends ReactorExecutionListener implements StartEventFromCallActivityListener {

    private static final Logger logger = LoggerFactory.getLogger(StartEventFromCallActivityListenerImpl.class);
    private final ProcessDocumentAssociationService processDocumentAssociationService;

    public StartEventFromCallActivityListenerImpl(ProcessDocumentAssociationService processDocumentAssociationService) {
        this.processDocumentAssociationService = processDocumentAssociationService;
    }

    @Override
    public void notify(DelegateExecution execution) {
        if (isExecutedFromCallActivity(execution)) {
            logger.info("Handling process started from CallActivity for process-definition-id - {}", execution.getProcessDefinitionId());
            final var parentProcessInstanceId = new CamundaProcessInstanceId(execution.getSuperExecution().getProcessInstanceId());
            processDocumentAssociationService
                .findProcessDocumentInstance(parentProcessInstanceId)
                .ifPresent(instance -> processDocumentAssociationService.createProcessDocumentInstance(
                    execution.getProcessInstanceId(), //processInstance from new process
                    UUID.fromString(instance.processDocumentInstanceId().documentId().toString()),
                    getProcessNameFrom(execution)
                ));
        }
    }

    private boolean isExecutedFromCallActivity(DelegateExecution execution) {
        return execution.getSuperExecution() != null;
    }

    private String getProcessNameFrom(DelegateExecution execution) {
        return execution
            .getBpmnModelInstance()
            .getDefinitions()
            .getRootElements()
            .stream()
            .filter(rootElement -> rootElement instanceof ProcessImpl)
            .filter(rootElement -> ((ProcessImpl) rootElement).isExecutable())
            .findFirst()
            .orElseThrow()
            .getAttributeValue("name");
    }

}