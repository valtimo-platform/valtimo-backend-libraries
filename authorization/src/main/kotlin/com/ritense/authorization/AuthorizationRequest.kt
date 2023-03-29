package com.ritense.authorization

class AuthorizationRequest<T>(
    val resourceType: String,
    val resources: List<String>? = listOf(),
    val action: Action,
    val classContext: Class<T>
)