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
import com.ritense.objectenapi.web.rest.result.ObjectDto
import com.ritense.objectenapi.web.rest.result.ObjecttypeDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID


@RestController
@RequestMapping(value = ["/api"])
class ZaakObjectResource(
    val zaakObjectService: ZaakObjectService,
) {
    @GetMapping(value = ["/v1/document/{documentId}/zaak/objecttype"])
    fun getZaakObjecttypes(
        @PathVariable(name = "documentId") documentId: UUID
    ): ResponseEntity<List<ObjecttypeDto>> {
        val zaakObjectTypes = zaakObjectService.getZaakObjectTypes(documentId).map {
            ObjecttypeDto(it.url, it.name)
        }
        return ResponseEntity.ok(zaakObjectTypes)
    }

    @GetMapping(value = ["/v1/document/{documentId}/zaak/object"])
    fun getZaakObjecten(
        @PathVariable(name = "documentId") documentId: UUID,
        @RequestParam(name = "typeUrl") typeUrl: URI
    ): ResponseEntity<List<Any>>{
        val objectDtos = zaakObjectService.getZaakObjectenOfType(documentId, typeUrl)
            .map(ObjectDto::create)
        return ResponseEntity.ok(objectDtos)
    }

    @GetMapping(value = ["/v1/document/{documentId}/zaak/object/form"])
    fun getZaakObjecten(
        @RequestParam(name = "objectUrl") objectUrl: URI
    ): ResponseEntity<FormDefinition>{
        val form = zaakObjectService.getZaakObjectForm(objectUrl)
        return form?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

}
