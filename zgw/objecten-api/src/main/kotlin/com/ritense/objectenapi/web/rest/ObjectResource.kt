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

package com.ritense.objectenapi.web.rest

import com.ritense.form.domain.FormDefinition
import com.ritense.objectenapi.service.ZaakObjectService
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RequestMapping(value = ["/api"])
class ObjectResource(
    val zaakObjectService: ZaakObjectService
) {

    @GetMapping(value = ["/v1/object/form"])
    fun getPrefilledObjectFromObjectUrl(
        @RequestParam(name = "objectUrl") objectUrl: URI
    ): ResponseEntity<FormDefinition> {
        val form = zaakObjectService.getZaakObjectForm(objectUrl)
        return form?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}