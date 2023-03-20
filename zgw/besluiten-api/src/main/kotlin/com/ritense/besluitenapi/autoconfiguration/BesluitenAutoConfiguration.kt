package com.ritense.besluitenapi.autoconfiguration

import com.ritense.besluitenapi.BesluitenApiPluginFactory
import com.ritense.besluitenapi.client.BesluitenApiClient
import com.ritense.plugin.service.PluginService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class BesluitenAutoConfiguration {

    @Bean
    fun besluitenApiClient(webclientBuilder: WebClient.Builder): BesluitenApiClient {
        return BesluitenApiClient(webclientBuilder)
    }

    @Bean
    fun besluitenApiPluginFactory(
        pluginService: PluginService,
        besluitenApiClient: BesluitenApiClient
    ): BesluitenApiPluginFactory {
        return BesluitenApiPluginFactory(pluginService, besluitenApiClient)
    }

}