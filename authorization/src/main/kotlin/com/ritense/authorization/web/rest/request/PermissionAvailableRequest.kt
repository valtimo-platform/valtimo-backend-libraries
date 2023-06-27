package com.ritense.authorization.web.rest.request

import com.fasterxml.jackson.annotation.JsonProperty

data class PermissionAvailableRequest(
    @JsonProperty("resource") var resourceAlias: String,
    var action: String,
    var context: PermissionContext
)