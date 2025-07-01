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
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId;
import com.ritense.processdocument.domain.listener.StartEventFromCallActivityListener;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.model.bpmn.impl.instance.ProcessImpl;
import org.springframework.context.event.EventListener;

public class StartEventFromCallActivityListenerImpl implements StartEventFromCallActivityListener {

    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final ProcessDocumentService processDocumentService;

    public StartEventFromCallActivityListenerImpl(
        ProcessDocumentAssociationService processDocumentAssociationService,
        ProcessDocumentService processDocumentService
    ) {
        this.processDocumentAssociationService = processDocumentAssociationService;
        this.processDocumentService = processDocumentService;
    }

    @RunWithoutAuthorization
    @EventListener(condition = "#execution.bpmnModelElementInstance != null " +
        "&& #execution.bpmnModelElementInstance.elementType.typeName == T(org.operaton.bpm.engine.ActivityTypes).START_EVENT " +
        "&& #execution.eventName == T(org.operaton.bpm.engine.delegate.ExecutionListener).EVENTNAME_START")
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
            var processId = new OperatonProcessInstanceId(execution.getSuperExecution().getProcessInstanceId());
            var documentId = processDocumentService.getDocumentId(processId, execution);
            if (documentId != null) {
                return documentId;
            }
        }
        var processId = new OperatonProcessInstanceId(execution.getProcessInstanceId());
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