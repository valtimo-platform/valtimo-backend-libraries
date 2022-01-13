package com.ritense.openzaak.domain.connector

import com.ritense.connector.domain.ConnectorProperties

data class OpenZaakProperties(
    var openZaakConfig: OpenZaakConfig = OpenZaakConfig()
) : ConnectorProperties {
}