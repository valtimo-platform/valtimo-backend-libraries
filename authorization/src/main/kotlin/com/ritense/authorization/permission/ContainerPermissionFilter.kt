package com.ritense.authorization.permission

class ContainerPermissionFilter(
    val entity: String,
    val filters: List<PermissionFilter>
): PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.CONTAINER
    override fun isValid(entity: Any): Boolean {
        TODO("Not yet implemented")
    }
}