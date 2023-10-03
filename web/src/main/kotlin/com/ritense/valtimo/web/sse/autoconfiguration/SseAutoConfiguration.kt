package com.ritense.valtimo.web.sse.autoconfiguration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.ritense.valtimo.web.sse.event.SseEventTypesResolver
import com.ritense.valtimo.web.sse.event.listener.SseEventDeploymentListener
import com.ritense.valtimo.web.sse.event.listener.SseEventListener
import com.ritense.valtimo.web.sse.messaging.MessagePublisher
import com.ritense.valtimo.web.sse.messaging.RedisMessagePublisher
import com.ritense.valtimo.web.sse.messaging.RedisMessageSubscriber
import com.ritense.valtimo.web.sse.security.config.SseHttpSecurityConfigurer
import com.ritense.valtimo.web.sse.service.SseSubscriptionService
import com.ritense.valtimo.web.sse.web.rest.SseResource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.GenericToStringSerializer


@Configuration
class SseAutoConfiguration {

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(SseHttpSecurityConfigurer::class)
    fun sseHttpSecurityConfigurer() = SseHttpSecurityConfigurer()

    @Bean
    @ConditionalOnMissingBean(SseSubscriptionService::class)
    fun sseSubscriptionService() = SseSubscriptionService()


    @Bean
    @ConditionalOnMissingBean(SseResource::class)
    fun camundaEventResource(
        sseSubscriptionService: SseSubscriptionService,
        redisMessagePublisher: RedisMessagePublisher
    ) = SseResource(sseSubscriptionService, redisMessagePublisher)

    @Bean
    fun redisMessagePublisher(): RedisMessagePublisher {
        return RedisMessagePublisher(redisTemplate(), topic())
    }

    @Bean
    fun redisMessageSubscriber(
        applicationEventPublisher: ApplicationEventPublisher,
        objectMapper: ObjectMapper
    ): RedisMessageSubscriber {
        return RedisMessageSubscriber(applicationEventPublisher, objectMapper)
    }

    @Bean
    fun messageListener(
        applicationEventPublisher: ApplicationEventPublisher,
        objectMapper: ObjectMapper
    ): MessageListenerAdapter {
        return MessageListenerAdapter(RedisMessageSubscriber(applicationEventPublisher, objectMapper))
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = jedisConnectionFactory()
        template.valueSerializer = GenericToStringSerializer(Any::class.java)
        return template
    }

    @Bean
    fun redisContainer(
        applicationEventPublisher: ApplicationEventPublisher,
        objectMapper: ObjectMapper
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory());
        container.addMessageListener(messageListener(applicationEventPublisher, objectMapper), topic());
        return container;
    }

    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic("pubsub:queue")
    }

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        return JedisConnectionFactory()
    }

    @Bean
    fun redisPublisher(): MessagePublisher {
        return RedisMessagePublisher(redisTemplate(), topic())
    }

    @Bean
    fun sseEventListener(sseSubscriptionService: SseSubscriptionService): SseEventListener {
        return SseEventListener(sseSubscriptionService)
    }

    @Bean
    fun sseEventTypesResolver(applicationContext: ApplicationContext): SseEventTypesResolver {
        return SseEventTypesResolver(applicationContext)
    }

    @Bean
    fun sseEventDeploymentListener(
        sseEventTypesResolver: SseEventTypesResolver,
        objectMapper: ObjectMapper
    ): SseEventDeploymentListener {
        return SseEventDeploymentListener(sseEventTypesResolver, objectMapper)
    }
}