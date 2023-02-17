package com.ritense.documentenapi.client

import com.fasterxml.jackson.annotation.JsonProperty

enum class ConfidentialityLevel {
    @JsonProperty("openbaar")
    OPENBAAR,
    @JsonProperty("beperkt_openbaar")
    BEPERKT_OPENBAAR,
    @JsonProperty("intern")
    INTERN,
    @JsonProperty("zaakvertrouwelijk")
    ZAAKVERTROUWELIJK,
    @JsonProperty("vertrouwelijk")
    VERTROUWELIJK,
    @JsonProperty("confidentieel")
    CONFIDENTIEEL,
    @JsonProperty("geheim")
    GEHEIM,
    @JsonProperty("zeer_geheim")
    ZEER_GEHEIM;


    val key: String
        get() = this.name.lowercase()

    companion object {
        fun fromKey(key: String?): ConfidentialityLevel? {
            return key?.let {
                ConfidentialityLevel.values().firstOrNull {
                    it.key == key.lowercase()
                }
            }
        }
    }
}
