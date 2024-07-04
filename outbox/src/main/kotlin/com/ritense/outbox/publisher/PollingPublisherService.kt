/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import com.ritense.outbox.ValtimoOutboxService
import mu.KotlinLogging
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.atomic.AtomicBoolean

open class PollingPublisherService(
    private val outboxService: ValtimoOutboxService,
    private val messagePublisher: MessagePublisher,
    private val platformTransactionManager: PlatformTransactionManager
) {
    private val polling = AtomicBoolean(false)

    init {
        logger.info { "Using ${messagePublisher::class.qualifiedName} as outbox message publisher." }
    }

    /**
     * Poll messages from the outbox table and publishes them in the correct order.
     */
    open fun pollAndPublishAll() {
        if (polling.compareAndSet(false, true)) {
            try {
                do {
                    TransactionTemplate(platformTransactionManager).executeWithoutResult {
                        val oldestMessage = outboxService.getOldestMessage()
                        if (oldestMessage != null) {
                            logger.debug { "Sending OutboxMessage '${oldestMessage.id}'" }
                            messagePublisher.publish(oldestMessage)
                            outboxService.deleteMessage(oldestMessage.id)
                        } else {
                            polling.set(false)
                        }
                    }
                } while (polling.get())
            } catch (e: Exception) {
                throw RuntimeException("Failed to poll and publish outbox messages", e)
            } finally {
                polling.set(false)
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
