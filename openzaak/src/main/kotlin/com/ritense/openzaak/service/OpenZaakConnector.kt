package com.ritense.openzaak.service

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType

@ConnectorType(name = "OpenZaak", allowMultipleConnectors = false)
class OpenZaakConnector(
    private var openZaakProperties: OpenZaakProperties
): Connector {
    override fun getProperties(): ConnectorProperties {
        return openZaakProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        openZaakProperties = connectorProperties as OpenZaakProperties
    }
}