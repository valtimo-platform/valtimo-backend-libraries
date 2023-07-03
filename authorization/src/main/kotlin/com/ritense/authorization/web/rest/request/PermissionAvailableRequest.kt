package com.ritense.authorization.web.rest.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class PermissionAvailableRequest(
    val resource: String,
    val action: String,
    val context: PermissionContext
) {
    @JsonIgnore
    fun getResourceAsClass(): Class<*> {
        return Class.forName(resource)
    }
}