/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.taak

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class TaakObjectDto(
    val bsn: String?,
    val kvk: String?,
    val identificatie: List<TaakIdentificatie> = listOf(),
    @JsonProperty("verwerker_taak_id") val verwerkerTaakId: UUID,
    @JsonProperty("formulier_id") val formulierId: String?,
    @JsonProperty("formulier_url") val formulierUrl: String?,
    val data: Map<String, Any>? = null,
    @JsonProperty("verzonden_data") val verzondenData: Map<String, Any>? = null,
    val status: TaakObjectStatus = TaakObjectStatus.open,
    val title: String? = null
) {
    init {
        require(kvk != null || bsn != null) { "BSN and/or KvK number is required!" }
        require(formulierId != null || formulierUrl != null) { "Form ID or Form URL is required!" }
    }
}

enum class TaakObjectStatus {
    open, ingediend, verwerkt, gesloten
}

data class TaakIdentificatie(
    val type: String,
    val value: String,
)
