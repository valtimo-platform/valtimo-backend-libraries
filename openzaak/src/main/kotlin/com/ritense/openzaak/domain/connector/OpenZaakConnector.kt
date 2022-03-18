package com.ritense.openzaak.domain.connector

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType

@ConnectorType(name = "OpenZaak", allowMultipleConnectors = false)
class OpenZaakConnector(
    private var openZaakProperties: OpenZaakProperties
): Connector {
    override fun getProperties(): OpenZaakProperties {
        return openZaakProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        openZaakProperties = connectorProperties as OpenZaakProperties
    }

    fun getName(): String {
        return "OpenZaak"
    }
}