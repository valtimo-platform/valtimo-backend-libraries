package com.ritense.besluitenapi

import com.ritense.plugin.service.PluginService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BesluitenApiAutoConfiguration {
    @Bean
    fun besluitenApiPluginFactory(pluginService: PluginService): BesluitenApiPluginFactory {
        return BesluitenApiPluginFactory(pluginService)
    }
}