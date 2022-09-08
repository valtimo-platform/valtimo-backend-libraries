package com.ritense.documentenapi.client

enum class ConfidentialityNotice(val key: String) {
    OPENBAAR("openbaar"),
    BEPERKT_OPENBAAR("beperkt_openbaar"),
    INTERN("intern"),
    ZAAKVERTROUWELIJK("zaakvertrouwelijk"),
    VERTROUWELIJK("vertrouwelijk"),
    CONFIDENTIEEL("confidentieel"),
    GEHEIM("geheim"),
    ZEER_GEHEIM("zeer_geheim");

    companion object {
        fun fromKey(key: String): ConfidentialityNotice {
            for (confidentialityNotice in ConfidentialityNotice.values()) {
                if (confidentialityNotice.key.equals(key, ignoreCase = true)) {
                    return confidentialityNotice
                }
            }
            throw IllegalStateException(String.format("Cannot create ConfidentialityNotice from key %s", key))
        }
    }
}
