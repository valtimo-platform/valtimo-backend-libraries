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

package com.ritense.openzaak.web.rest

import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.web.rest.response.ZaakInstanceLinkDTO
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping(value = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ZaakInstanceLinkResource(
    val zaakInstanceLinkService: ZaakInstanceLinkService,
) {

    @GetMapping(value = ["/v1/zaakinstancelink/zaak"])
    fun getZaakInstanceLink(
        @RequestParam(name = "zaakInstanceUrl") zaakInstanceUrl: URI
    ): ResponseEntity<ZaakInstanceLinkDTO> {
        val entity = zaakInstanceLinkService.getByZaakInstanceUrl(zaakInstanceUrl)
        return ResponseEntity.ok(
            ZaakInstanceLinkDTO(
                zaakInstanceUrl = entity.zaakInstanceUrl,
                documentId = entity.documentId
            )
        )
    }

    @GetMapping(value = ["/v1/zaakinstancelink/document"])
    fun getZaakInstanceLink(
        @RequestParam(name = "documentId") documentId: UUID
    ): ResponseEntity<ZaakInstanceLinkDTO> {
        val entity = zaakInstanceLinkService.getByDocumentId(documentId)
        return ResponseEntity.ok(
            ZaakInstanceLinkDTO(
                zaakInstanceUrl = entity.zaakInstanceUrl,
                documentId = entity.documentId
            )
        )
    }
}
