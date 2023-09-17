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

package com.ritense.valtimo.web.sse.web.rest

import com.ritense.valtimo.web.sse.domain.Subscriber
import com.ritense.valtimo.web.sse.service.SseSubscriptionService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class SseResource(
    private val sseSubscriptionService: SseSubscriptionService
) {

    @GetMapping("/api/v1/sse")
    fun subscribeToEvents() = sseSubscriptionService.subscribe()

    @GetMapping("/api/v1/sse/{subscriptionId}")
    fun subscribeToEvents(
        @PathVariable subscriptionId: UUID?
    ): Subscriber {
        subscriptionId?.let {
            logger.info { "subscribe (re-use ${subscriptionId})" }
        } ?: logger.info { "SSE Subscribe (new subscription)" }
        return sseSubscriptionService.subscribe(subscriptionId)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}