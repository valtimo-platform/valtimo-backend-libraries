package com.ritense.authorization

class AuthorizationRequest<T>(
    val resources: Map<String, List<String>>?,
    val action: Action,
    val classContext: Class<T>
)