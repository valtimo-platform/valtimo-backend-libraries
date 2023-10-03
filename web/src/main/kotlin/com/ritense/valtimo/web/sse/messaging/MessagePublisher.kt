package com.ritense.valtimo.web.sse.messaging

interface MessagePublisher {
    fun publish(message: String?)
}