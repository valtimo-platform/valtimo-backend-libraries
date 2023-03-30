package com.ritense.authorization.permission

class FieldPermissionFilter(
    val field: String,
    val value: String
) : PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.FIELD
    override fun isValid(entity: Any): Boolean {
        return reflectionFindField(entity).toString() == value
    }

    private fun reflectionFindField(entity: Any): Any {
        var currentEntity = entity
        field.split('.').forEach {
                val declaredField = currentEntity.javaClass.getDeclaredField(it)
                declaredField.trySetAccessible()
                currentEntity = declaredField.get(currentEntity)
        }
        return currentEntity
    }
}