package com.ritense.exact.plugin

import com.ritense.exact.service.ExactService
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.service.PluginService
import org.springframework.context.ApplicationContext
import org.springframework.web.client.RestClient

class ExactPluginFactory(
    pluginService: PluginService,
    private val exactService: ExactService,
    private val exactClient: RestClient,
    private val context: ApplicationContext
) : PluginFactory<ExactPlugin>(pluginService) {

    override fun create(): ExactPlugin = ExactPlugin(exactService, exactClient, context)

}