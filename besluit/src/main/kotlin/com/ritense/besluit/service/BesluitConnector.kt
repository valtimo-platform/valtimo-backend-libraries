package com.ritense.besluit.service

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType

@ConnectorType(name = "BesluitApi")
class BesluitConnector(
    besluitApiProperties: BesluitApiProperties
) : Connector, BesluitenService(besluitApiProperties) {

    override fun getProperties(): BesluitApiProperties {
        return besluitApiProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        besluitApiProperties = connectorProperties as BesluitApiProperties
    }

    companion object {
        const val rootUrlApiVersion = "/api/v1"
    }
}