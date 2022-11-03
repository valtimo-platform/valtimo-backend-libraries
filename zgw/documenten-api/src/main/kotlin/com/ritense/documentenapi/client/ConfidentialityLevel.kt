package com.ritense.documentenapi.client

enum class ConfidentialityLevel(val key: String) {
    OPENBAAR("openbaar"),
    BEPERKT_OPENBAAR("beperkt_openbaar"),
    INTERN("intern"),
    ZAAKVERTROUWELIJK("zaakvertrouwelijk"),
    VERTROUWELIJK("vertrouwelijk"),
    CONFIDENTIEEL("confidentieel"),
    GEHEIM("geheim"),
    ZEER_GEHEIM("zeer_geheim");

    companion object {
        fun fromKey(key: String): ConfidentialityLevel {
            return ConfidentialityLevel.valueOf(key.uppercase())
        }
    }
}
