package com.ritense.authorization.permission

abstract class PermissionFilter {
    abstract val permissionFilterType: PermissionFilterType
    abstract fun isValid(entity: Any): Boolean
}