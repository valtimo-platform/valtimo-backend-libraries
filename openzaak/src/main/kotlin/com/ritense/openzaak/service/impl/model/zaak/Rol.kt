/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.service.impl.model.zaak

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.ritense.openzaak.service.impl.model.zaak.betrokkene.BetrokkeneIdentificatie
import com.ritense.openzaak.service.impl.model.zaak.betrokkene.RolNatuurlijkPersoon
import com.ritense.openzaak.service.impl.model.zaak.betrokkene.RolNietNatuurlijkPersoon
import java.net.URI

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Rol(
    val zaak: URI,
    val betrokkene: URI?,
    val betrokkeneType: BetrokkeneType,
    val roltype: URI,
    val roltoelichting: String,
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "betrokkeneType",
        visible = true
    )
    @JsonSubTypes(
        value = [
            JsonSubTypes.Type(value = RolNatuurlijkPersoon::class, name = "natuurlijk_persoon"),
            JsonSubTypes.Type(value = RolNietNatuurlijkPersoon::class, name = "niet_natuurlijk_persoon")
    ])
    val betrokkeneIdentificatie: BetrokkeneIdentificatie?
)