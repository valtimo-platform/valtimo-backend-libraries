package com.ritense.outbox.publisher

import com.ritense.outbox.domain.OutboxMessage
import mu.KotlinLogging

open class DefaultMessagePublisher : MessagePublisher {
    override fun publish(message: OutboxMessage) {
        logger.info { "publish $message" }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}