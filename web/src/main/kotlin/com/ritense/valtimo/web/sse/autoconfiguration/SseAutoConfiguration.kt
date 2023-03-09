package com.ritense.valtimo.web.sse.autoconfiguration

import com.ritense.valtimo.web.sse.security.config.SseHttpSecurityConfigurer
import com.ritense.valtimo.web.sse.service.SseSubscriptionService
import com.ritense.valtimo.web.sse.web.rest.SseResource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

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
        sseSubscriptionService: SseSubscriptionService
    ) = SseResource(sseSubscriptionService)
}