package com.ritense.openproduct.service

import com.ritense.openproduct.plugin.OpenProductPlugin
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val pluginService: PluginService
) {

    fun doSomething(input: String ): String {
        println(getPlugin().baseUrl)
        return "Something - $input"
    }

    fun getPlugin() : OpenProductPlugin {
        val pluginConfiguration = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "openproduct",
            )
        ).first()
        return pluginService.createInstance(pluginConfiguration) as OpenProductPlugin
    }
}