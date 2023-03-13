package com.ritense.besluitenapi

import com.ritense.plugin.annotation.Plugin
import mu.KLogger
import mu.KotlinLogging

@Plugin(key = BesluitenApiPlugin.PLUGIN_KEY,
    title = "Besluiten API",
    description = "Connects to the Besluiten API")
class BesluitenApiPlugin {
    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        const val PLUGIN_KEY = "besluitenapi"
    }
}