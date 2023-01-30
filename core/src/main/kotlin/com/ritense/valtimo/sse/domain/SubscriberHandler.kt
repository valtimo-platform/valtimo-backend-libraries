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

package com.ritense.valtimo.sse.domain

import com.ritense.valtimo.sse.event.BaseSseEvent
import java.io.IOException
import java.util.LinkedList

class SubscriberHandler(
    val state: SubscriberState = SubscriberState()
) {

    /**
     * Active subscriber instance if connected, or null if disconnected
     */
    var subscriber: Subscriber? = null
        private set

    private val eventQueue = LinkedList<BaseSseEvent>()

    /**
     * Sends an SSE event, or if not connected add it to the queue, to be sent upon reconnecting
     */
    fun enqueue(event: BaseSseEvent) {
        var sent = false
        this.subscriber?.let {
            try {
                it.send(event)
                sent = true
            } catch (ex: IOException) {
                this.subscriber = null
            }
        }
        if (!sent) {
            this.eventQueue.add(event)
        }
    }

    fun setSubscriber(subscriber: Subscriber?) {
        this.subscriber = subscriber
        // process built up events
        subscriber?.let { sub ->
            while (eventQueue.isNotEmpty()) {
                val event = eventQueue.pop()
                try {
                    sub.send(event)
                } catch (ex: IOException) {
                    // reinsert event at start of queue
                    eventQueue.add(0, event)
                    this.subscriber = null
                    break
                }
            }
        }
    }

}