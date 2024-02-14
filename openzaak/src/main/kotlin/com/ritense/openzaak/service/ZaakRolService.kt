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

package com.ritense.openzaak.service

import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.zaak.Rol
import java.net.URI

@Deprecated("Since 12.0.0. Replace with the Zaken API plugin")
interface ZaakRolService {

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.ZakenApiClient.createZaakRol"))
    fun addNatuurlijkPersoon(zaakUrl: URI, roltoelichting: String, roltype: URI, bsn: String, betrokkene: URI?)

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.ZakenApiClient.createZaakRol"))
    fun addNietNatuurlijkPersoon(zaakUrl: URI, roltoelichting: String, roltype: URI, kvk: String, betrokkene: URI?)

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.ZakenApiClient.getZaakRollen"))
    fun getZaakInitator(zaakUrl: URI): ResultWrapper<Rol>

}