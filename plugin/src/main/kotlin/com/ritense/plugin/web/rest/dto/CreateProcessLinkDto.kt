package com.ritense.plugin.web.rest.dto

open class CreateProcessLinkDto (
    val pluginConfigurationKey: String,
    val pluginActionDefinitionKey: String,
    val actionProperties: String
)