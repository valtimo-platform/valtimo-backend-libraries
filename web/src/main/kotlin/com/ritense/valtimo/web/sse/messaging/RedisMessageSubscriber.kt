package com.ritense.valtimo.web.sse.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.ritense.valtimo.web.sse.event.BaseSseEvent
import com.ritense.valtimo.web.sse.event.RedisMessageReceived
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener

class RedisMessageSubscriber(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper
) : MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        System.out.println("Message received: " + message.toString())
        messageList.add(message.toString())
        val baseSseEvent = objectMapper.readValue(message.body, BaseSseEvent::class.java)
        baseSseEvent?.let {
            applicationEventPublisher.publishEvent(baseSseEvent)
        }
    }

    companion object {
        var messageList: MutableList<String> = ArrayList()
    }
}