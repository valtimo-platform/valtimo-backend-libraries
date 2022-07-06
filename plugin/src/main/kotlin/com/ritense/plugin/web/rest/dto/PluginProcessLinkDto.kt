package com.ritense.plugin.web.rest.dto

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID

/**
 * TODO: inherit from a ProcessLink?
 */
data class PluginProcessLinkDto (
    val id: UUID? = null,
    val processDefinitionId: String? = null,
    val activityId: String? = null,
    val pluginConfigurationKey: String,
    val pluginActionDefinitionKey: String,
    val actionProperties: JsonNode
)