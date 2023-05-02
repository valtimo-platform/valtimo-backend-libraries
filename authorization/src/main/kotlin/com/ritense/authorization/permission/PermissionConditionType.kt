package com.ritense.authorization.permission

import com.fasterxml.jackson.annotation.JsonValue

// TODO: consider not making this an enum
enum class PermissionConditionType {
    CONTAINER,
    FIELD,
    EXPRESSION;

    val value: String
        @JsonValue get() = name.lowercase()
}