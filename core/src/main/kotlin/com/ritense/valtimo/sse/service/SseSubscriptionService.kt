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

package com.ritense.valtimo.sse.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.ritense.valtimo.sse.domain.Subscriber
import com.ritense.valtimo.sse.domain.SubscriberHandler
import com.ritense.valtimo.sse.event.BaseSseEvent
import com.ritense.valtimo.sse.event.EstablishedConnectionSseEvent
import mu.KotlinLogging
import java.time.Duration
import java.util.UUID

class SseSubscriptionService {

    // use cache so items get automatically evicted after 3h as final safe-guard for memory leaks
    private val subscriberHandles: Cache<UUID, SubscriberHandler> = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofHours(3))
        .build()

    fun subscribe(subscriptionId: UUID? = null): Subscriber {
        logger.debug { "Fetching new or existing subscription: $subscriptionId" }
        return subscriptionId?.let { id ->
            subscriberHandles.getIfPresent(id)?.let { handle ->
                logger.debug { "Found state for $id, getting subscription" }
                ensureConnectedSubscriber(handle)
            }
        } ?: registerNewSubscriber()
    }

    fun remove(subscriptionId: UUID) {
        this.subscriberHandles.invalidate(subscriptionId)
    }

    fun notifySubscribers(event: BaseSseEvent) {
        logger.debug { "Notify subscribers (total=${subscriberHandles.asMap().size})" }
        try {
            subscriberHandles.asMap().values.forEach { subscriber ->
                logger.debug { "Sending notification to ${subscriber.state.subscriptionId}" }
                subscriber.enqueue(event)
            }
        } catch (e: Exception) {
            logger.error { "Failed to notify subscribers $e" }
        }
    }

    private fun registerNewSubscriber(): Subscriber {
        return this.setupNewSubscriber(SubscriberHandler(), true)
    }

    private fun setupNewSubscriber(handle: SubscriberHandler, isNew: Boolean): Subscriber {
        val subscriber = Subscriber()
        handle.setSubscriber(subscriber)
        if (isNew) {
            // not removing items from the map, counting on auto cache evict to do so, so that a browser can reconnect
            this.subscriberHandles.put(handle.state.subscriptionId, handle)
            // send subscription id to client
            handle.enqueue(EstablishedConnectionSseEvent(handle.state.subscriptionId))
        }

        // On Client connection timeout, unregister and mark complete subscriber
        subscriber.onTimeout {
            logger.debug { "subscriber timeout" }
            subscriber.complete()
            handle.setSubscriber(null)
        }
        subscriber.onError {
            logger.debug { "subscriber errored ${it.message}" }
            subscriber.completeWithError(it)
            handle.setSubscriber(null)
        }
        return subscriber
    }

    // if connection is lost, the state can be kept, but a new Subscriber + SSE connection has to be made and bound to it
    private fun ensureConnectedSubscriber(handle: SubscriberHandler): Subscriber {
        return handle.subscriber ?: this.setupNewSubscriber(handle, false)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}