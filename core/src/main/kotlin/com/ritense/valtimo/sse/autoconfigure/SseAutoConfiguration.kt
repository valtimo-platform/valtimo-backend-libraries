package com.ritense.valtimo.sse.autoconfigure

import com.ritense.valtimo.sse.domain.listener.TaskUpdateListener
import com.ritense.valtimo.sse.security.config.SseHttpSecurityConfigurer
import com.ritense.valtimo.sse.service.SseSubscriptionService
import com.ritense.valtimo.sse.web.rest.CamundaEventResource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
class SseAutoConfiguration {

    @Bean
    @Order(270)
    fun sseHttpSecurityConfigurer() = SseHttpSecurityConfigurer()

    @Bean
    fun sseSubscriptionService() = SseSubscriptionService()

    @Bean
    fun taskUpdateListener(
        sseSubscriptionService: SseSubscriptionService
    ) = TaskUpdateListener(sseSubscriptionService)

    @Bean
    fun camundaEventResource(
        sseSubscriptionService: SseSubscriptionService
    ) = CamundaEventResource(sseSubscriptionService)

}