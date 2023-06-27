package com.ritense.authorization.web.rest.result

data class PermissionAvailableResult(
    var resource: String,
    var action: String,
    var context: Map<String, String>,
    var available: Boolean
)