package com.ritense.plugin.web.rest.dto.processlink

import com.fasterxml.jackson.databind.JsonNode

data class PluginProcessLinkCreateDto(
    val processDefinitionId: String,
    val activityId: String,
    val pluginConfigurationKey: String,
    val pluginActionDefinitionKey: String,
    val actionProperties: JsonNode
)
