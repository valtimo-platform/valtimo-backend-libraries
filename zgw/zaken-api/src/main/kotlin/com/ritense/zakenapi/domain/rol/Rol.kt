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

package com.ritense.zakenapi.domain.rol

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Rol(
    val url: URI? = null,
    val uuid: UUID? = null,
    val zaak: URI,
    val betrokkene: URI? = null,
    val betrokkeneType: BetrokkeneType,
    val roltype: URI,
    val omschrijving: String? = null,
    val omschrijvingGeneriek: ZaakRolOmschrijving? = null,
    val roltoelichting: String,
    val registratiedatum: LocalDateTime? = null,
    val indicatieMachtiging: IndicatieMachtiging? = null,
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "betrokkeneType",
        visible = true
    )
    val betrokkeneIdentificatie: BetrokkeneIdentificatie?
)