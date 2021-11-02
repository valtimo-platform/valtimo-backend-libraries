/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

@Slf4j
@RequiredArgsConstructor
public class TaskEventHandler {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * A 'complete' event is triggered when a task has finished normally.
     * When a task is interrupted by another trigger (for example by a boundary timer event)
     * a task event is dispatched with a 'delete' name
     *
     * This event handler acts on both event types to tell the portal to delete its task as well
     */
    @EventListener(condition = "#taskDelegate.eventName=='delete' || #taskDelegate.eventName=='complete'")
    public void onTaskEvent(DelegateTask taskDelegate) {
        logger.debug("Received a '{}' event for the task '{}'",
            taskDelegate.getEventName(), taskDelegate.getName());

        // Dispatch a DeleteTaskPortalEvent
        applicationEventPublisher.publishEvent(
            new DeletePortalTaskEvent(taskDelegate.getId())
        );
    }
}
