package com.ritense.plugin.web.rest.dto

import com.fasterxml.jackson.databind.JsonNode

class PluginConfiguration(
    val title: String,
    val properties: JsonNode,
    val definitionKey: String
)