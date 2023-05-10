package com.ritense.authorization.permission

data class ConditionContainer(
    val conditions: List<PermissionCondition> = emptyList(),
)