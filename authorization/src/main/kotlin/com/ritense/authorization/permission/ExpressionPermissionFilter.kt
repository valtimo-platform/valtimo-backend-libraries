package com.ritense.authorization.permission

class ExpressionPermissionFilter(
    val field: String,
    val path: String,
    val operator: String,
    val value: String
): PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.EXPRESSION
}