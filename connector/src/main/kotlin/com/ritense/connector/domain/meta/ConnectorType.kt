package com.ritense.connector.domain.meta

@Deprecated("Since 12.0.0", ReplaceWith("com.ritense.plugin.annotation.Plugin"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ConnectorType(val name: String = "", val allowMultipleConnectors: Boolean = true)