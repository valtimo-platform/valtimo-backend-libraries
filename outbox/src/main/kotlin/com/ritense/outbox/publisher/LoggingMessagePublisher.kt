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

import com.ritense.outbox.OutboxMessage
import mu.KotlinLogging

// TODO: Remove this MessagePublisher when Valtimo has another MessagePublisher out of the box.
open class LoggingMessagePublisher : MessagePublisher {

    override fun publish(message: OutboxMessage) {
        logger.debug { "OutboxMessage id: '${message.id}'" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
