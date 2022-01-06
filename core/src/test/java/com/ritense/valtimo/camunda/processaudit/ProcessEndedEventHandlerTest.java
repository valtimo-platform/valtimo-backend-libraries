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
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ProcessEndedEventHandlerTest {

    private ApplicationEventPublisher applicationEventPublisher;

    private ProcessEndedEventHandler processEndedEventHandler;

    @BeforeEach
    void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        processEndedEventHandler = new ProcessEndedEventHandler(applicationEventPublisher);
    }

    @Test
    void shouldPublishEvent() {
        String origin = RequestHelper.getOrigin();
        String user = AuditHelper.getActor();
        String processDefinitionId = "prodDefId";
        String processInstanceId = "procInstId";
        String businessKey = "businessKey";

        HistoricProcessInstanceEventEntity historyEvent = new HistoricProcessInstanceEventEntity();
        historyEvent.setEventType("end");
        historyEvent.setProcessDefinitionId(processDefinitionId);
        historyEvent.setProcessInstanceId(processInstanceId);
        historyEvent.setBusinessKey(businessKey);

        processEndedEventHandler.handleEvent(historyEvent);

        ArgumentCaptor<ProcessEndedEvent> argumentCaptor = ArgumentCaptor.forClass(ProcessEndedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getId()).isNotNull();
        assertThat(argumentCaptor.getValue().getOrigin()).isEqualTo(origin);
        assertThat(argumentCaptor.getValue().getOccurredOn()).isNotNull();
        assertThat(argumentCaptor.getValue().getUser()).isEqualTo(user);
        assertThat(argumentCaptor.getValue().getProcessDefinitionId()).isEqualTo(processDefinitionId);
        assertThat(argumentCaptor.getValue().getProcessInstanceId()).isEqualTo(processInstanceId);
        assertThat(argumentCaptor.getValue().getBusinessKey()).isEqualTo(businessKey);
    }


    @Test
    void shouldNotPublishEventIncorrectEventClass() {
        HistoryEvent historyEvent = new HistoricTaskInstanceEventEntity();

        processEndedEventHandler.handleEvent(historyEvent);

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldNotPublishEventIncorrectEventType() {
        HistoryEvent historyEvent = new HistoricProcessInstanceEventEntity();
        historyEvent.setEventType("start");

        processEndedEventHandler.handleEvent(historyEvent);

        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
