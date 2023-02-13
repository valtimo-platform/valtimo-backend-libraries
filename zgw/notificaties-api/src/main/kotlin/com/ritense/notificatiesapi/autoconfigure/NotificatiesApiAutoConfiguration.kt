/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.notificatiesapi.autoconfigure

import com.ritense.notificatiesapi.NotificatiesApiPluginFactory
import com.ritense.notificatiesapi.client.NotificatiesApiClient
import com.ritense.notificatiesapi.repository.NotificatiesApiAbonnementLinkRepository
import com.ritense.notificatiesapi.security.config.NotificatiesApiHttpSecurityConfigurer
import com.ritense.notificatiesapi.service.NotificatiesApiService
import com.ritense.notificatiesapi.web.rest.NotificatiesApiResource
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.service.PluginService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.notificatiesapi.repository"])
@EntityScan("com.ritense.notificatiesapi.domain")
class NotificatiesApiAutoConfiguration {

    @Bean
    fun notificatiesApiClient(webclientBuilder: WebClient.Builder): NotificatiesApiClient {
        return NotificatiesApiClient(webclientBuilder)
    }

    @Bean
    @ConditionalOnMissingBean(NotificatiesApiPluginFactory::class)
    fun notificatiesApiPluginFactory(
        pluginService: PluginService,
        pluginConfigurationRepository: PluginConfigurationRepository,
        client: NotificatiesApiClient,
        abonnementLinkRepository: NotificatiesApiAbonnementLinkRepository
    ): NotificatiesApiPluginFactory {
        return NotificatiesApiPluginFactory(
            pluginService,
            client,
            abonnementLinkRepository
        )
    }

    @Bean
    fun notificatiesApiService(
        applicationEventPublisher: ApplicationEventPublisher,
        notificatiesApiAbonnementLinkRepository: NotificatiesApiAbonnementLinkRepository
    ): NotificatiesApiService {
        return NotificatiesApiService(applicationEventPublisher, notificatiesApiAbonnementLinkRepository)
    }

    @Bean
    fun notificatiesApiResource(notificatiesApiService: NotificatiesApiService): NotificatiesApiResource {
        return NotificatiesApiResource(notificatiesApiService)
    }

    @Bean
    @Order(270)
    fun notificatiesApiHttpSecurityConfigurer(): NotificatiesApiHttpSecurityConfigurer {
        return NotificatiesApiHttpSecurityConfigurer()
    }
}