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

package com.ritense.openzaak.web.rest.impl

import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.impl.model.catalogi.InformatieObjectType
import com.ritense.openzaak.web.rest.InformatieObjectTypeResource
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import java.util.UUID

class InformatieObjectTypeResource(val zaakService: ZaakService) : InformatieObjectTypeResource {

    override fun get(catalogus: UUID): ResponseEntity<Collection<InformatieObjectType?>> {
        val objectInformatieTypen = zaakService.getInformatieobjecttypes(catalogus)
        return if (objectInformatieTypen.isNotEmpty()) {
            ok(objectInformatieTypen)
        } else notFound().build()
    }

}