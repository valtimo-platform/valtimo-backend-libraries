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

package com.ritense.openzaak.service.impl

import com.ritense.openzaak.service.ZaakRolService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.EigenschapType
import com.ritense.openzaak.service.impl.model.zaak.BetrokkeneType
import com.ritense.openzaak.service.impl.model.zaak.Rol
import com.ritense.openzaak.service.impl.model.zaak.RolNatuurlijkPersoon
import com.ritense.openzaak.service.impl.model.zaak.Zaak
import java.net.URI
import org.springframework.web.client.RestTemplate

class ZaakRolService(
    private val restTemplate: RestTemplate,
    private val openZaakConfigService: OpenZaakConfigService,
    private val tokenGeneratorService: OpenZaakTokenGeneratorService
): ZaakRolService {

    override fun addNatuurlijkPersoon(zaakUrl: URI, roltoelichting: String, roltype: URI, bsn: String, betrokkene: URI?) {
        OpenZaakRequestBuilder(restTemplate, openZaakConfigService, tokenGeneratorService)
            .path("zaken/api/v1/rollen")
            .body(Rol(
                zaakUrl,
                betrokkene,
                BetrokkeneType.NATUURLIJK_PERSOON,
                roltype,
                roltoelichting,
                RolNatuurlijkPersoon(bsn)
            ))
            .post()
            .build()
            .execute(Rol::class.java)
    }

    override fun getZaakInitator(zaakUrl: URI): ResultWrapper<Rol> {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, tokenGeneratorService)
            .path("zaken/api/v1/rollen")
            .queryParams(mapOf(
                "zaak" to zaakUrl.toString(),
                "omschrijvingGeneriek" to "initiator"
            ))
            .get()
            .build()
            .executeWrapped(Rol::class.java)
    }

}