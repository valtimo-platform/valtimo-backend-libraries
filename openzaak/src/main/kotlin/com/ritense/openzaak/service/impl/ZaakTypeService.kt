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

import com.ritense.openzaak.service.ZaakTypeService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.ZaakType
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
@SkipComponentScan
class ZaakTypeService(
    private val restTemplate: RestTemplate,
    private val openZaakConfigService: OpenZaakConfigService,
    private val openZaakTokenGeneratorService: OpenZaakTokenGeneratorService
) : ZaakTypeService {

    @Deprecated("Since 12.0.0")
    override fun getZaakTypes(): ResultWrapper<ZaakType> {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("catalogi/api/v1/zaaktypen")
            .get()
            .build()
            .executeWrapped(ZaakType::class.java)
    }

    override fun getZaakType(zaaktypeUrl: URI): ZaakType {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path(zaaktypeUrl.path)
            .get()
            .build()
            .execute(ZaakType::class.java)
    }
}