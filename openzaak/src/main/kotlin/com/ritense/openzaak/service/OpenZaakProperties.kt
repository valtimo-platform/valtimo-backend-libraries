package com.ritense.openzaak.service

import com.ritense.connector.domain.ConnectorProperties

data class OpenZaakProperties(
    var openZaakConfig: OpenZaakConfig = OpenZaakConfig()
) : ConnectorProperties {
}