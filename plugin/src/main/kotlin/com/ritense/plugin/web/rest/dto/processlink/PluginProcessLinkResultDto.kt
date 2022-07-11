package com.ritense.plugin.web.rest.dto.processlink

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID

data class PluginProcessLinkResultDto (
    val id: UUID,
    val processDefinitionId: String,
    val activityId: String,
    val pluginConfigurationKey: String,
    val pluginActionDefinitionKey: String,
    val actionProperties: JsonNode
)