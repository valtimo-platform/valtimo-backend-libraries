package com.ritense.authorization

// TODO: Don't make this an enum, make these values that can be stored in the DB
enum class Action {
    VIEW,
    CREATE_INSTANCE,
    ASSIGN,
    CLAIM,
    COMPLETE,
    LIST_VIEW,
    IGNORE
}
