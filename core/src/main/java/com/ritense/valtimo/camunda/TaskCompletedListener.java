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

import com.ritense.valtimo.contract.audit.utils.AuditHelper;
import com.ritense.valtimo.contract.event.TaskCompletedEvent;
import com.ritense.valtimo.contract.utils.RequestHelper;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorTaskListener;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@CamundaSelector(type = ActivityTypes.TASK_USER_TASK, event = TaskListener.EVENTNAME_COMPLETE)
public class TaskCompletedListener extends ReactorTaskListener {

    private final ApplicationEventPublisher applicationEventPublisher;

    public TaskCompletedListener(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        applicationEventPublisher.publishEvent(
            new TaskCompletedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                delegateTask.getAssignee(),
                LocalDateTime.ofInstant(delegateTask.getCreateTime().toInstant(), ZoneId.systemDefault()),
                delegateTask.getId(),
                delegateTask.getName(),
                delegateTask.getProcessDefinitionId(),
                delegateTask.getProcessInstanceId(),
                delegateTask.getVariablesTyped(),
                delegateTask.getExecution().getProcessBusinessKey()
            )
        );
    }

}