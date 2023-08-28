package com.ritense.exact.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.exact.plugin.ExactPluginFactory
import com.ritense.exact.security.ExactPluginSecurityConfigurer
import com.ritense.exact.service.ExactService
import com.ritense.exact.web.rest.ExactResource
import com.ritense.plugin.service.PluginService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ExactPluginAutoConfiguration {

    @Bean
    fun exactPluginFactory(
        pluginService: PluginService,
        exactClient: WebClient,
        exactService: ExactService,
        context: ApplicationContext
    ): ExactPluginFactory {
        return ExactPluginFactory(pluginService, exactService, exactClient, context)
    }

    @Bean
    fun exactClient(@Value("\${exact.baseUrl}") baseUrl: String): WebClient {
        return WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeader("Accept", "application/json")
            .build()
    }

    @Bean
    fun exactService(
        @Value("\${exact.redirectUrl}") redirectUrl: String,
        exactClient: WebClient,
        pluginService: PluginService,
        objectMapper: ObjectMapper
    ): ExactService {
        return ExactService(redirectUrl, exactClient, pluginService, objectMapper)
    }

    @Bean
    fun exactResource(exactService: ExactService): ExactResource {
        return ExactResource(exactService)
    }

    @Order(420)
    @Bean
    @ConditionalOnMissingBean(ExactPluginSecurityConfigurer::class)
    fun exactPluginHttpSecurityConfigurer(): ExactPluginSecurityConfigurer {
        return ExactPluginSecurityConfigurer()
    }

}