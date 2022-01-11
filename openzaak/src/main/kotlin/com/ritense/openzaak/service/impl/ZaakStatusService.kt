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

import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.service.ZaakStatusService
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.StatusType
import java.net.URI
import org.springframework.web.client.RestTemplate

class ZaakStatusService(
    private val restTemplate: RestTemplate,
    private val openZaakConfigService: OpenZaakConfigService,
    private val tokenGeneratorService: OpenZaakTokenGeneratorService,
    private val documentService: DocumentService,
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService
) : ZaakStatusService {

    override fun getStatusTypes(zaaktype: URI): ResultWrapper<StatusType> {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, tokenGeneratorService)
            .path("catalogi/api/v1/statustypen")
            .get()
            .queryParams(mapOf("zaaktype" to zaaktype.toString(), "status" to "alles"))
            .build()
            .executeWrapped(StatusType::class.java)
    }

    override fun getStatusType(statusTypeUrl: URI): StatusType? {
        val statusTypePath = openZaakConfigService.getOpenZaakConfig()?.let { statusTypeUrl.toString().replace(it.url, "") }
        return statusTypePath?.let {
            OpenZaakRequestBuilder(restTemplate, openZaakConfigService, tokenGeneratorService)
                .path(it)
                .get()
                .build()
                .execute(StatusType::class.java)
        }
    }

    override fun setStatus(documentId: Document.Id, status: String) {
        val document = documentService.findBy(documentId).orElseThrow()
        val zaakTypeLink = zaakTypeLinkService.findBy(document.definitionId().name())
        val zaakInstanceUrl = zaakInstanceLinkService.getByDocumentId(documentId.id).zaakInstanceUrl
        val statusUri = getStatusTypeByOmschrijving(zaakTypeLink.zaakTypeUrl, status).url!!
        zaakTypeLink.assignZaakInstanceStatus(zaakInstanceUrl, statusUri)
        zaakTypeLinkService.modify(zaakTypeLink)
    }

    private fun getStatusTypeByOmschrijving(zaaktype: URI, omschrijving: String): StatusType {
        return getStatusTypes(zaaktype).results.stream()
            .filter { it.omschrijving == omschrijving }
            .findFirst().get()
    }
}