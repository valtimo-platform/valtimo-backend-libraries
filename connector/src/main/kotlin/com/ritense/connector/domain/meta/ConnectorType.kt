package com.ritense.connector.domain.meta

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ConnectorType(val name: String = "") {
}