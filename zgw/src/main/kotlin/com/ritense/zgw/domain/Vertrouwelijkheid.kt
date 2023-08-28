/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.zgw.domain

import com.fasterxml.jackson.annotation.JsonProperty

enum class Vertrouwelijkheid() {
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
        fun fromKey(key: String?): Vertrouwelijkheid? {
            return key?.let {
                Vertrouwelijkheid.values().firstOrNull {
                    it.key == key.lowercase()
                }
            }
        }
    }
}