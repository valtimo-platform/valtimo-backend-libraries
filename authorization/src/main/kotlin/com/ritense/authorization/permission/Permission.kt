package com.ritense.authorization.permission

import com.ritense.authorization.Action

class Permission(
    val resourceType: String,
    val action: Action,
    val filters: List<PermissionFilter>
)