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

package com.ritense.openzaak.catalogi

import com.ritense.openzaak.service.impl.OpenZaakConfigService
import com.ritense.openzaak.service.impl.OpenZaakRequestBuilder
import com.ritense.openzaak.service.impl.OpenZaakTokenGeneratorService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.BesluitType
import java.net.URI
import org.springframework.web.client.RestTemplate

class CatalogiClient(
    private val restTemplate: RestTemplate,
    private val openZaakConfigService: OpenZaakConfigService,
    private val openZaakTokenGeneratorService: OpenZaakTokenGeneratorService
) {

    fun getBesluittypen(catalogusUrl: URI): ResultWrapper<BesluitType> {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("catalogi/api/v1/besluittypen")
            .queryParams(mapOf("catalogus" to catalogusUrl.toString()))
            .get()
            .build()
            .executeWrapped(BesluitType::class.java)
    }

}