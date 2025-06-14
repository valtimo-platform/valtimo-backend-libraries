package com.ritense.openproduct.client

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Enum representing the status of a product.
 */
enum class StatusEnum {
    @JsonProperty("initieel")
    INITIEEL,

    @JsonProperty("gereed")
    GEREED,

    @JsonProperty("actief")
    ACTIEF,

    @JsonProperty("ingetrokken")
    INGETROKKEN,

    @JsonProperty("geweigerd")
    GEWEIGERD,

    @JsonProperty("verlopen")
    VERLOPEN
}
