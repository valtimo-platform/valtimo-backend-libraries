package com.ritense.objectsapi.web.rest.request

import java.util.UUID

data class CreateObjectSyncConfigRequest(
    val connectorInstanceId: UUID,
    val enabled: Boolean,
    val documentDefinitionName: String,
    val objectTypeId: UUID
)
