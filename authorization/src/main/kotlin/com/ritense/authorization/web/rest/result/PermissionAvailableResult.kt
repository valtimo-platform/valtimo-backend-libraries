package com.ritense.authorization.web.rest.result

import com.ritense.authorization.web.rest.request.PermissionContext

data class PermissionAvailableResult(
    var resource: String,
    var action: String,
    var context: PermissionContext,
    var available: Boolean
)