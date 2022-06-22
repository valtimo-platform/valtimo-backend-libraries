package com.ritense.plugin.service

import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.repository.PluginDefinitionRepository

class PluginService(
    private var pluginDefinitionRepository: PluginDefinitionRepository
) {

    fun getPluginDefinitions(): MutableList<PluginDefinition> {
        return pluginDefinitionRepository.findAll()
    }
}