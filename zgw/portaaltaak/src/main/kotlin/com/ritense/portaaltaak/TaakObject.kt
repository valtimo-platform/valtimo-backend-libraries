/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.portaaltaak

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.net.URI
import java.time.LocalDateTime

class TaakObject(
    val identificatie: TaakIdentificatie,
    val data: Map<String, Any>,
    val title: String,
    val status: TaakStatus,
    val formulier: TaakForm,
    @JsonProperty("verwerker_taak_id")
    val verwerkerTaakId: String,
    @JsonProperty("zaak")
    val zaakUrl: URI?,
    val verloopdatum: LocalDateTime?,
    @JsonProperty("verzonden_data")
    val verzondenData: Map<String, Any> = mapOf(),
)

class TaakIdentificatie(
    val type: String,
    val value: String
) {
    companion object {
        const val TYPE_BSN = "bsn"
        const val TYPE_KVK = "kvk"
    }
}

class TaakForm(
    val type: TaakFormType,
    val value: String
)

enum class TaakStatus(@JsonValue val key: String) {
    OPEN("open"),
    INGEDIEND("ingediend"),
    VERWERKT("verwerkt"),
    GESLOTEN("gesloten")
}

enum class TaakFormType(@JsonValue val key: String) {
    ID("id"),
    URL("url")
}
