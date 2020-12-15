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

import com.ritense.openzaak.service.EigenschapService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.EigenschapType
import org.springframework.web.client.RestTemplate
import java.net.URI

class EigenschapService(
    private val restTemplate: RestTemplate,
    private val openZaakConfigService: OpenZaakConfigService,
    private val tokenGeneratorService: OpenZaakTokenGeneratorService
) : EigenschapService {

    override fun getEigenschappen(zaakType: URI): ResultWrapper<EigenschapType> {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, tokenGeneratorService)
            .path("catalogi/api/v1/eigenschappen")
            .get()
            .queryParams(mapOf("zaaktype" to zaakType.toString(), "status" to "alles"))
            .build()
            .executeWrapped(EigenschapType::class.java)
    }

}