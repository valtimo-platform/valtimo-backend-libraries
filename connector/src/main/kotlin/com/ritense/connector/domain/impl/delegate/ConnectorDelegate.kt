package com.ritense.connector.domain.impl.delegate

import com.ritense.connector.repository.ConnectorTypeInstanceRepository
import mu.KotlinLogging

class ConnectorDelegate(
    val connectorTypeInstanceRepository: ConnectorTypeInstanceRepository,
) {


    companion object {
        val logger = KotlinLogging.logger {}
    }
}