package com.ritense.openproduct.service

import com.ritense.openproduct.client.OpenProductClient
import com.ritense.openproduct.client.Product
import com.ritense.openproduct.client.ProductResponse
import com.ritense.openproduct.plugin.OpenProductPlugin
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
import org.springframework.stereotype.Service
import kotlin.reflect.full.memberProperties

@Service
class ProductService(
    private val pluginService: PluginService,
    private val pluginClient: OpenProductClient
) {

    fun doSomething(uuid: String, propertyName: String): String {
        val product = pluginClient.getAllProducts(
            getPlugin().baseUrl,
            getPlugin().authenticationPluginConfiguration
        )?.last()

        val property = ProductResponse::class.memberProperties.find { it.name == propertyName }
            ?: throw IllegalArgumentException("Property $propertyName not found in Product class")

        return property.get(product!!)?.toString() ?: ""
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
