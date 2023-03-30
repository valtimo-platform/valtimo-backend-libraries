package com.ritense.authorization

class AuthorizationRequest<T>(
    val resourceType: Class<T>,
    val resources: List<String>? = listOf(),
    val action: Action,
)