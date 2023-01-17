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

package com.ritense.valtimo.camunda.processaudit;

import com.ritense.valtimo.contract.audit.utils.AuditHelper;
import com.ritense.valtimo.contract.utils.RequestHelper;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProcessStartedEventHandler implements HistoryEventHandler {

    private final ApplicationEventPublisher applicationEventPublisher;

    public ProcessStartedEventHandler(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
        if (historyEvent instanceof HistoricProcessInstanceEventEntity && "start".equals(historyEvent.getEventType())) {
            final String processDefinitionId = historyEvent.getProcessDefinitionId();
            final String processInstanceId = historyEvent.getProcessInstanceId();
            final String businessKey = ((HistoricProcessInstanceEventEntity) historyEvent).getBusinessKey();
            final String processDefinitionKey = historyEvent.getProcessDefinitionKey();

            applicationEventPublisher.publishEvent(
                new ProcessStartedEvent(
                    UUID.randomUUID(),
                    RequestHelper.getOrigin(),
                    LocalDateTime.now(),
                    AuditHelper.getActor(),
                    processDefinitionId,
                    processInstanceId,
                    businessKey,
                    processDefinitionKey
                )
            );
        }
    }

    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
        historyEvents.forEach(this::handleEvent);
    }

}
