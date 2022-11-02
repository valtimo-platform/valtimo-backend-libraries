package com.ritense.exact.plugin

import com.ritense.exact.service.ExactService
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.service.PluginService
import org.springframework.context.ApplicationContext
import org.springframework.web.reactive.function.client.WebClient

class ExactPluginFactory(
    pluginService: PluginService,
    private val exactService: ExactService,
    private val exactClient: WebClient,
    private val context: ApplicationContext
) :
    PluginFactory<ExactPlugin>(pluginService) {

    override fun create(): ExactPlugin {
        return ExactPlugin(exactService, exactClient, context)
    }

}