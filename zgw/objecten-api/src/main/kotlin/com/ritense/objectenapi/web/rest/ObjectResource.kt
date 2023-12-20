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

package com.ritense.objectenapi.web.rest

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.form.domain.FormDefinition
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.service.ZaakObjectService
import com.ritense.objectenapi.web.rest.result.FormType
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URI
import java.util.UUID

@Controller
@RequestMapping("/api/v1/object", produces = [APPLICATION_JSON_UTF8_VALUE])
class ObjectResource(
    private val zaakObjectService: ZaakObjectService
) {

    @GetMapping("/form")
    fun getPrefilledObjectFromObjectUrl(
        @RequestParam(name = "objectUrl") objectUrl: URI? = null,
        @RequestParam(name = "objectManagementId") objectManagementId: UUID? = null,
        @RequestParam(name = "objectId") objectId: UUID? = null,
        @RequestParam(name = "formType") formType: FormType? = null
    ): ResponseEntity<FormDefinition> {
        val form = zaakObjectService.getZaakObjectForm(objectUrl, objectManagementId, objectId, formType)
        return form?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @PatchMapping
    fun patchObject(
        @RequestParam(name = "objectManagementId") objectManagementId: UUID,
        @RequestParam(name = "objectId") objectId: UUID,
        @RequestBody jsonNode: JsonNode
    ): ResponseEntity<URI> {
        return ResponseEntity.ok(zaakObjectService.patchObjectFromManagementId(objectManagementId, objectId, jsonNode))
    }

    @GetMapping
    fun getObjectByUrl(
        @RequestParam(name = "objectUrl") objectUrl: URI): ResponseEntity<ObjectWrapper?> =
         ResponseEntity.ok(zaakObjectService.getObjectByObjectUrl(objectUrl))

}
