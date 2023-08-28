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

package com.ritense.valtimo.web.sse.service

import com.ritense.valtimo.web.sse.event.BaseSseEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.mock
import java.util.UUID

class SseSubscriptionServiceTest {

    private val sseSubscriptionService: SseSubscriptionService = SseSubscriptionService()

    @Test
    fun `subscribe without subscriptionId`() {
        val subscriber = sseSubscriptionService.subscribe()
        assertThat(subscriber).isNotNull
    }

    @Test
    fun `subscribe with existing subscriptionId`() {
        val subscriptionId = UUID.randomUUID()
        val subscriber = sseSubscriptionService.subscribe(subscriptionId)
        assertThat(subscriber).isNotNull
    }

    @Test
    fun notifySubscribers() {
        val subscriptionId = UUID.randomUUID()
        val event = mock<BaseSseEvent>()
        sseSubscriptionService.subscribe(subscriptionId)
        assertDoesNotThrow {
            sseSubscriptionService.notifySubscribers(
                event
            )
        }
    }

    @Test
    fun remove() {
        val subscriptionId = UUID.randomUUID()
        sseSubscriptionService.subscribe(subscriptionId)
        assertDoesNotThrow {
            sseSubscriptionService.remove(subscriptionId)
        }
    }

}
