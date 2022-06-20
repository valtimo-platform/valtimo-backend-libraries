package com.ritense.plugin

class PluginDefinitionNotDeployedException(
    val pluginKey: String,
    val fullyQualifiedClassName: String,
    cause: Throwable?
) : Exception("Unable to deploy plugin with key \'$pluginKey\' and class name \'$fullyQualifiedClassName\'", cause)