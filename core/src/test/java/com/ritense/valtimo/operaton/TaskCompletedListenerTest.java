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

package com.ritense.valtimo.operaton;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ritense.valtimo.contract.event.TaskCompletedEvent;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.DelegateTask;
import org.operaton.bpm.engine.variable.VariableMap;
import org.springframework.context.ApplicationEventPublisher;

class TaskCompletedListenerTest {

    private ApplicationEventPublisher applicationEventPublisher;
    private TaskCompletedListener taskCompletedListener;
    private ArgumentCaptor<TaskCompletedEvent> taskCompletedEventCaptor;
    private DelegateTask delegateTask;

    @BeforeEach
    void setup() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        taskCompletedListener = new TaskCompletedListener(applicationEventPublisher);
        taskCompletedEventCaptor = ArgumentCaptor.forClass(TaskCompletedEvent.class);

        delegateTask = mock(DelegateTask.class);
        when(delegateTask.getId()).thenReturn("taskId");
        when(delegateTask.getName()).thenReturn("name");
        when(delegateTask.getCreateTime()).thenReturn(new Date());
        when(delegateTask.getProcessDefinitionId()).thenReturn("processDefinitionId");
        when(delegateTask.getProcessInstanceId()).thenReturn("processInstanceId");
        when(delegateTask.getVariables()).thenReturn(mock(VariableMap.class));
        when(delegateTask.getExecution()).thenReturn(mock(DelegateExecution.class));
    }

    @Test
    void shouldPublishTaskCompletedEventWhenDelegateTaskIsCompleted() {
        taskCompletedListener.notify(delegateTask);
        verify(applicationEventPublisher, times(1)).publishEvent(taskCompletedEventCaptor.capture());
    }

}
