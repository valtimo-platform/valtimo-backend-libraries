/*
 *  Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.sse.domain.listener

import com.ritense.valtimo.sse.event.TaskUpdateSseEvent
import com.ritense.valtimo.web.sse.service.SseSubscriptionService
import org.camunda.bpm.spring.boot.starter.event.TaskEvent
import org.springframework.transaction.event.TransactionalEventListener

class TaskUpdateListener(
    private val sseSubscriptionService: SseSubscriptionService
) {

    @TransactionalEventListener(
        condition = "#taskEvent.eventName=='create' " +
                "|| #taskEvent.eventName=='complete' " +
                "|| #taskEvent.eventName=='delete'"
        ,
        fallbackExecution = true
    )
    fun handle(taskEvent: TaskEvent) {
        sseSubscriptionService.notifySubscribers(
            TaskUpdateSseEvent(
                processInstanceId = taskEvent.processInstanceId
            )
        )
    }

}