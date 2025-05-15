package com.ritense.objectsapi.opennotificaties

import com.ritense.objectsapi.domain.request.HandleNotificationRequest

data class OpenNotificationEvent(
    val notification: HandleNotificationRequest,
    val connectorId: String,
    val authorizationKey: String,
)