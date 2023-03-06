/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeletePortalTaskEventHandlerTest {

    private static final String TASK_ID = "taskId";
    private ApplicationEventPublisher applicationEventPublisher;
    private TaskEventHandler taskEventHandler;
    private DelegateTask delegateTask;

    @BeforeEach
    void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        taskEventHandler = new TaskEventHandler(applicationEventPublisher);

        delegateTask = mock(DelegateTask.class);
        when(delegateTask.getId()).thenReturn(TASK_ID);
        when(delegateTask.getName()).thenReturn("This is a task name");
    }

    @Test
    void shouldPublishEventOnDeleteTaskEvent() {
        when(delegateTask.getEventName()).thenReturn("delete");

        // "Publish" the TaskEvent
        taskEventHandler.onTaskEvent(delegateTask);

        ArgumentCaptor<DeletePortalTaskEvent> argumentCaptor = ArgumentCaptor.forClass(DeletePortalTaskEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getTaskId()).isNotNull();
        assertThat(argumentCaptor.getValue().getTaskId()).isEqualTo(TASK_ID);
    }

    @Test
    void shouldPublishEventOnCompleteTaskEvent() {
        when(delegateTask.getEventName()).thenReturn("complete");

        // Publish the TaskEvent
        taskEventHandler.onTaskEvent(delegateTask);

        ArgumentCaptor<DeletePortalTaskEvent> argumentCaptor = ArgumentCaptor.forClass(DeletePortalTaskEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getTaskId()).isNotNull();
        assertThat(argumentCaptor.getValue().getTaskId()).isEqualTo(TASK_ID);
    }
}
