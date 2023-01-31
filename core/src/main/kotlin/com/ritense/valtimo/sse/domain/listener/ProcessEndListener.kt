/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.ritense.valtimo.sse.event.ProcessEndSseEvent
import com.ritense.valtimo.sse.service.SseSubscriptionService
import org.camunda.bpm.spring.boot.starter.event.ExecutionEvent
import org.springframework.transaction.event.TransactionalEventListener

class ProcessEndListener(
    private val sseSubscriptionService: SseSubscriptionService
) {

    @TransactionalEventListener(
        condition = "#executionEvent.eventName=='end'",
        fallbackExecution = true
    )
    fun handle(executionEvent: ExecutionEvent) {
        sseSubscriptionService.notifySubscribers(
            ProcessEndSseEvent(
                processInstanceId = executionEvent.processInstanceId
            )
        )
    }

}