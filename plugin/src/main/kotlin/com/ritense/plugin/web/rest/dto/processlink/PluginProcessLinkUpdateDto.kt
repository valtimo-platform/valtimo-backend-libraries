package com.ritense.plugin.web.rest.dto.processlink

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID

data class PluginProcessLinkUpdateDto(
    val id: UUID,
    val pluginConfigurationId: UUID,
    val pluginActionDefinitionKey: String,
    val actionProperties: JsonNode? = null
)
