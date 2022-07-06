package com.ritense.plugin.web.rest.dto

import java.util.UUID

/**
 * TODO: inherit from a ProcessLink?
 */
data class PluginProcessLinkDto (
    val id: UUID?,
    val processDefinitionId: String?,
    val activityId: String?,
    val pluginConfigurationKey: String,
    val pluginActionDefinitionKey: String,
    val actionProperties: String
)