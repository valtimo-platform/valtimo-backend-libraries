package com.ritense.plugin.service

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.plugin.domain.ActivityType
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginDefinition
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.domain.ProcessLink
import com.ritense.plugin.repository.PluginActionDefinitionRepository
import com.ritense.plugin.repository.PluginConfigurationRepository
import com.ritense.plugin.repository.PluginDefinitionRepository
import com.ritense.plugin.repository.PluginProcessLinkRepository
import com.ritense.plugin.web.rest.dto.PluginActionDefinitionDto
import com.ritense.plugin.web.rest.dto.PluginProcessLinkDto

class PluginService(
    private var pluginDefinitionRepository: PluginDefinitionRepository,
    private var pluginConfigurationRepository: PluginConfigurationRepository,
    private var pluginActionDefinitionRepository: PluginActionDefinitionRepository,
    private var pluginProcessLinkRepository: PluginProcessLinkRepository,
) {

    fun getPluginDefinitions(): List<PluginDefinition> {
        return pluginDefinitionRepository.findAll()
    }

    fun getPluginConfigurations(): List<PluginConfiguration> {
        return pluginConfigurationRepository.findAll()
    }

    fun getPluginConfiguration(key: String): PluginConfiguration {
        return pluginConfigurationRepository.getById(key)
    }

    fun createPluginConfiguration(
        title: String,
        properties: JsonNode,
        pluginDefinitionKey: String
    ): PluginConfiguration {
        val pluginDefinition = pluginDefinitionRepository.getById(pluginDefinitionKey)

        return pluginConfigurationRepository.save(
            PluginConfiguration(PluginConfigurationId.newId(), title, properties, pluginDefinition)
        )
    }

    fun getPluginDefinitionActions(
        pluginDefinitionKey: String,
        activityType: ActivityType?
    ): List<PluginActionDefinitionDto> {
        val actions = if (activityType == null)
            pluginActionDefinitionRepository.findByIdPluginDefinitionKey(pluginDefinitionKey)
        else
            pluginActionDefinitionRepository.findByIdPluginDefinitionKeyAndActivityTypes(pluginDefinitionKey, activityType)

        return actions.map {
            PluginActionDefinitionDto(
                it.id.key,
                it.title,
                it.description
            )
        }
    }

    fun getProcessLinks(
        processDefinitionId: String,
        activityId: String
    ): List<ProcessLink> {
        return pluginProcessLinkRepository.findByProcessDefinitionIdAndActivityId(processDefinitionId, activityId)
    }

    fun createProcessLink(processLink: PluginProcessLinkDto) {
        requireNotNull(processLink.processDefinitionId) //TODO: throw some ValidationExceptions?
        requireNotNull(processLink.activityId)

        val newProcessLink = PluginProcessLink(
            id = PluginProcessLinkId.newId(),
            processDefinitionId = processLink.processDefinitionId,
            activityId = processLink.activityId,
            actionProperties = processLink.actionProperties,
            pluginConfigurationKey = processLink.pluginConfigurationKey,
            pluginActionDefinitionKey = processLink.pluginActionDefinitionKey
        )
        pluginProcessLinkRepository.save(newProcessLink)
    }

    fun updateProcessLink(processLink: PluginProcessLinkDto) {
        requireNotNull(processLink.id)  //TODO: throw some ValidationException?

        val link = pluginProcessLinkRepository.getById(
            PluginProcessLinkId.existingId(processLink.id)
        ).copy(
                actionProperties = processLink.actionProperties,
                pluginConfigurationKey = processLink.pluginConfigurationKey,
                pluginActionDefinitionKey = processLink.pluginActionDefinitionKey
            )
        pluginProcessLinkRepository.save(link)
    }
}
