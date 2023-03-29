package com.ritense.authorization.permission

class FieldPermissionFilter(
    val field: String,
    val value: String
) : PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.FIELD
}