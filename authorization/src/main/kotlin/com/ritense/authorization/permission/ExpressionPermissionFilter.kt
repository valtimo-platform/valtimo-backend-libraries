package com.ritense.authorization.permission

import com.jayway.jsonpath.JsonPath


class ExpressionPermissionFilter(
    val field: String,
    val path: String,
    val operator: ExpressionOperator,
    val value: String
): PermissionFilter() {
    override val permissionFilterType: PermissionFilterType = PermissionFilterType.EXPRESSION
    override fun isValid(entity: Any): Boolean {
        val fieldValue = reflectionFindFieldIfString(entity) ?: return false

        return evaluateExpression(
            JsonPath.read(fieldValue, path)
        )
    }

    private fun reflectionFindFieldIfString(entity: Any): String? {
        var currentEntity = entity
        field.split('.').forEach {
            val declaredField = currentEntity.javaClass.getDeclaredField(it)
            declaredField.trySetAccessible()
            currentEntity = declaredField.get(currentEntity)
        }
        return if (currentEntity is String) {
            currentEntity as String
        } else {
            null
        }
    }

    private fun evaluateExpression(pathValue: String): Boolean {
        return operator.evaluate(pathValue, value)
    }
}