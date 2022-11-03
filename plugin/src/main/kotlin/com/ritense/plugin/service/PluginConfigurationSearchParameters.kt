package com.ritense.plugin.service

import com.ritense.plugin.domain.ActivityType

class PluginConfigurationSearchParameters(
    val category: String? = null,
    val activityType: ActivityType? = null)