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

package com.ritense.openzaak.web.rest.impl

import com.ritense.openzaak.service.impl.ZaakTypeService
import com.ritense.openzaak.service.impl.model.catalogi.ZaakType
import com.ritense.openzaak.web.rest.ZaakTypeResource
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok

class ZaakTypeResource(
    private val zaakTypeService: ZaakTypeService
) : ZaakTypeResource {

    override fun getZaakTypes(): ResponseEntity<List<ZaakType>> {
        return ok(zaakTypeService.getZaakTypes().results.toList())
    }

}