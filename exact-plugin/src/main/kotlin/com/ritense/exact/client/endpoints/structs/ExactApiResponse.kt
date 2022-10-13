package com.ritense.exact.client.endpoints.structs

import com.fasterxml.jackson.annotation.JsonProperty

abstract class ExactApiResponse<T: Any> {

    @JsonProperty("d")
    lateinit var data: T

}