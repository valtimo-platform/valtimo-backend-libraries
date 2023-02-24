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
import com.ritense.objectenapi.service.ZaakObjectService
import com.ritense.objectenapi.web.rest.result.ObjectDto
import com.ritense.objectenapi.web.rest.result.ObjecttypeDto
import com.ritense.plugin.service.PluginService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URI
import java.util.UUID

@RequestMapping(value = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ZaakObjectResource(
    val zaakObjectService: ZaakObjectService,
    val pluginService: PluginService
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

    @Deprecated(
        message = "The documentId is not mandatory anymore",
        replaceWith = ReplaceWith("api/v1/object/form")
    )
    @GetMapping(value = ["/v1/document/{documentId}/zaak/object/form"])
    fun getZaakObjecten(
        @RequestParam(name = "objectUrl") objectUrl: URI
    ): ResponseEntity<FormDefinition>{
        val form = zaakObjectService.getZaakObjectForm(objectUrl)
        return form?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @PostMapping(value = ["/v1/object"])
    fun createZaakObject(
        @RequestParam(name = "objectManagementId") objectManagementId: UUID,
        @RequestBody data: JsonNode
    ): ResponseEntity<Any> {
        val objectUrl = zaakObjectService.createObject(objectManagementId, data)
        return objectUrl.let {
            ResponseEntity.status(HttpStatus.CREATED).body(
                mapOf(
                    "url" to it
                )
            )
        } ?: ResponseEntity.notFound().build()
    }

    @PutMapping(value = ["/v1/object"])
    fun updateZaakObject(
        @RequestParam(name = "objectManagementId") objectManagementId: UUID,
        @RequestParam(name = "objectUrl") objectUrl: URI,
        @RequestBody data: JsonNode
    ): ResponseEntity<Any> {
        val updateObjectUrl = zaakObjectService.updateObject(objectManagementId, objectUrl, data)
        return updateObjectUrl.let {
            ResponseEntity.status(HttpStatus.CREATED).body(
                mapOf(
                    "url" to it
                )
            )
        } ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping(value = ["/v1/object"])
    fun deleteZaakObject(
        @RequestParam(name = "objectManagementId") objectManagementId: UUID,
        @RequestParam(name = "objectId") objectId: UUID? = null,
        @RequestParam(name = "objectUrl") objectUrl: URI? = null,
    ): ResponseEntity<Any> {
        val status = zaakObjectService.deleteObject(objectManagementId, objectId, objectUrl)
        return ResponseEntity.status(status).build()
    }
}
