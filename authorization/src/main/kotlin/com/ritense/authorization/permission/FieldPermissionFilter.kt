package com.ritense.authorization.permission

import java.lang.reflect.Field

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
            currentEntity = currentEntity.javaClass.getField(it).get(currentEntity)
        }
        return currentEntity
    }
}