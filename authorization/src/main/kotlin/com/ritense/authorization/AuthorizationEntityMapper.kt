package com.ritense.authorization

interface AuthorizationEntityMapper<FROM, TO> {
    fun mapTo(toClass: Class<TO>, entity: FROM): List<TO>

    fun appliesTo(fromClass: Class<FROM>, toClass: Class<TO>)
}