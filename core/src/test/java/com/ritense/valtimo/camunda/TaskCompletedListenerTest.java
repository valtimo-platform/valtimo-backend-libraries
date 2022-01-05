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

package com.ritense.valtimo.camunda;

import com.ritense.valtimo.contract.event.TaskCompletedEvent;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;

import static org.camunda.bpm.extension.mockito.CamundaMockito.delegateExecutionFake;
import static org.camunda.bpm.extension.mockito.CamundaMockito.delegateTaskFake;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        delegateTask = delegateTaskFake()
            .withId("taskId")
            .withName("name")
            .withCreateTime(new Date())
            .withProcessDefinitionId("processDefinitionId")
            .withProcessInstanceId("processInstanceId")
            .withVariables(mock(VariableMap.class))
            .withExecution(delegateExecutionFake());
    }

    @Test
    void shouldPublishTaskCompletedEventWhenDelegateTaskIsCompleted() {
        taskCompletedListener.notify(delegateTask);
        verify(applicationEventPublisher, times(1)).publishEvent(taskCompletedEventCaptor.capture());
    }

}
