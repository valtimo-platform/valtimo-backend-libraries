package com.ritense.plugin

class PluginDefinitionNotDeployedException(
    pluginName: String,
    fullyQualifiedClassName: String,
    cause: Throwable?
) : Exception("Unable to deploy plugin with name \'$pluginName\' and class name \'$fullyQualifiedClassName\'", cause)