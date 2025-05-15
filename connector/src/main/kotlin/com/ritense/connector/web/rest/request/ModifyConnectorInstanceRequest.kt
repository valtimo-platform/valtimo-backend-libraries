package com.ritense.connector.web.rest.request

import com.ritense.connector.domain.ConnectorProperties
import java.util.UUID

@Deprecated("Since 12.0.0")
data class ModifyConnectorInstanceRequest(
    val id: UUID,
    val typeId: UUID,
    val name: String,
    val connectorProperties: ConnectorProperties
)