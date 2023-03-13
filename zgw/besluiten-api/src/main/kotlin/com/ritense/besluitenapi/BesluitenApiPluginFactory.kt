package com.ritense.besluitenapi

import com.ritense.plugin.PluginFactory
import com.ritense.plugin.service.PluginService

class BesluitenApiPluginFactory(
    pluginService: PluginService
): PluginFactory<BesluitenApiPlugin>(pluginService) {
    override fun create(): BesluitenApiPlugin {
        return BesluitenApiPlugin()
    }
}