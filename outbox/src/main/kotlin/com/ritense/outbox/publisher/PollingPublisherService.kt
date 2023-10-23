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

package com.ritense.outbox.publisher

import com.ritense.outbox.OutboxService
import mu.KotlinLogging
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.atomic.AtomicBoolean

open class PollingPublisherService(
    private val outboxService: OutboxService,
    private val messagePublisher: MessagePublisher,
    private val platformTransactionManager: PlatformTransactionManager
) {
    private val polling = AtomicBoolean(false)

    /**
     * Poll messages from the outbox table and publishes them in the correct order.
     */
    open fun pollAndPublishAll() {
        if (polling.compareAndSet(false, true)) {
            do {
                try {
                    TransactionTemplate(platformTransactionManager).executeWithoutResult {
                        val oldestMessage = outboxService.getOldestMessage()
                        if (oldestMessage != null) {
                            logger.info("Sending message '${oldestMessage.id}': ${oldestMessage.message}")
                            messagePublisher.publish(oldestMessage)
                            outboxService.deleteMessage(oldestMessage.id)
                        } else {
                            polling.set(false)
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Failed to poll and publish outbox messages", e)
                }
            } while (polling.get())
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
