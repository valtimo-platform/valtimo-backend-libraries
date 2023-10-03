package com.ritense.valtimo.web.sse.messaging

import com.ritense.valtimo.web.sse.event.BaseSseEvent
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic


class RedisMessagePublisher(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val topic: ChannelTopic
) : MessagePublisher {
    override fun publish(message: String?) {
        redisTemplate.convertAndSend(topic.topic, message)
    }
}