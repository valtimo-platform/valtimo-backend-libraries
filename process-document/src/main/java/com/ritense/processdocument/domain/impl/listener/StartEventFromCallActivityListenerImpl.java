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

import com.ritense.authorization.annotation.RunWithoutAuthorization;
import com.ritense.document.domain.Document;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.listener.StartEventFromCallActivityListener;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener;
import org.camunda.bpm.model.bpmn.impl.instance.ProcessImpl;

@CamundaSelector(type = ActivityTypes.START_EVENT, event = ExecutionListener.EVENTNAME_START)
public class StartEventFromCallActivityListenerImpl extends ReactorExecutionListener implements StartEventFromCallActivityListener {

    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final ProcessDocumentService processDocumentService;

    public StartEventFromCallActivityListenerImpl(
        ProcessDocumentAssociationService processDocumentAssociationService,
        ProcessDocumentService processDocumentService
    ) {
        this.processDocumentAssociationService = processDocumentAssociationService;
        this.processDocumentService = processDocumentService;
    }

    @Override
    @RunWithoutAuthorization
    public void notify(DelegateExecution execution) {
        Document.Id documentId = getDocumentId(execution);
        if (documentId != null) {
            processDocumentAssociationService.createProcessDocumentInstance(
                execution.getProcessInstanceId(), //processInstance from new process
                documentId.getId(),
                getProcessNameFrom(execution)
            );
        }
    }

    private Document.Id getDocumentId(DelegateExecution execution) {
        if (execution.getSuperExecution() != null) {
            var processId = new CamundaProcessInstanceId(execution.getSuperExecution().getProcessInstanceId());
            var documentId = processDocumentService.getDocumentId(processId, execution);
            if (documentId != null) {
                return documentId;
            }
        }
        var processId = new CamundaProcessInstanceId(execution.getProcessInstanceId());
        return processDocumentService.getDocumentId(processId, execution);
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