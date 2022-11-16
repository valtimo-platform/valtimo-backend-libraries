package com.ritense.exact.client.endpoints.structs

import com.fasterxml.jackson.annotation.JsonProperty

class ExactApiResultList<T : Any>() {

    @JsonProperty("results")
    lateinit var results: Array<T>

}