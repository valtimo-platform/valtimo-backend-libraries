/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.sse.autoconfigure

import com.ritense.valtimo.sse.domain.listener.ProcessEndListener
import com.ritense.valtimo.sse.domain.listener.TaskUpdateListener
import com.ritense.valtimo.sse.security.config.SseHttpSecurityConfigurer
import com.ritense.valtimo.sse.service.SseSubscriptionService
import com.ritense.valtimo.sse.web.rest.CamundaEventResource
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
    @ConditionalOnMissingBean(TaskUpdateListener::class)
    fun taskUpdateListener(
        sseSubscriptionService: SseSubscriptionService
    ) = TaskUpdateListener(sseSubscriptionService)

    @Bean
    @ConditionalOnMissingBean(ProcessEndListener::class)
    fun processEndListener(
        sseSubscriptionService: SseSubscriptionService
    ) = ProcessEndListener(sseSubscriptionService)

    @Bean
    @ConditionalOnMissingBean(CamundaEventResource::class)
    fun camundaEventResource(
        sseSubscriptionService: SseSubscriptionService
    ) = CamundaEventResource(sseSubscriptionService)

}