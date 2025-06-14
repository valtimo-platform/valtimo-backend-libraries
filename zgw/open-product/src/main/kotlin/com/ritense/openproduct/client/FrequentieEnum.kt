package com.ritense.openproduct.client

import com.fasterxml.jackson.annotation.JsonProperty

enum class FrequentieEnum {
    @JsonProperty("eenmalig")
    EENMALIG,

    @JsonProperty("jaarlijks")
    JAARLIJKS,

    @JsonProperty("maandelijks")
    MAANDELIJKS
}
