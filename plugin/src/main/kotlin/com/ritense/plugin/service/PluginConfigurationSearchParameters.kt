package com.ritense.plugin.service

import com.ritense.plugin.domain.ActivityType

class PluginConfigurationSearchParameters(
    val pluginDefinitionKey: String? = null,
    val pluginConfigurationTitle: String? = null,
    val category: String? = null,
    val activityType: ActivityType? = null,
)
