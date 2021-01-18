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

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.service.ZaakService
import com.ritense.openzaak.service.impl.ZaakService.Constants.Companion.DATE_PATTERN
import com.ritense.openzaak.service.impl.ZaakService.Constants.Companion.DATE_TIME_FORMAT
import com.ritense.openzaak.service.impl.model.zaak.Eigenschap
import com.ritense.openzaak.service.impl.model.zaak.Zaak
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class ZaakService(
    private val restTemplate: RestTemplate,
    private val openZaakConfigService: OpenZaakConfigService,
    private val openZaakTokenGeneratorService: OpenZaakTokenGeneratorService,
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val documentService: DocumentService
) : ZaakService {

    override fun createZaakWithLink(delegateExecution: DelegateExecution) {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString(delegateExecution.processBusinessKey))
        val document = documentService.findBy(documentId).orElseThrow()
        val openZaakConfig = openZaakConfigService.get()!!

        val zaakTypeLink = zaakTypeLinkService.findBy(document.definitionId().name())
        val zaakInstance = createZaak(
            zaakTypeLink.zaakTypeUrl,
            LocalDateTime.now(),
            openZaakConfig.rsin.value
        )
        zaakTypeLinkService.assignZaakInstance(
            zaakTypeLink.zaakTypeLinkId,
            ZaakInstanceLink(zaakInstance.url, zaakInstance.uuid, documentId.id)
        )
    }

    override fun createZaak(
        zaaktype: URI,
        startdatum: LocalDateTime,
        rsin: String
    ): Zaak {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("zaken/api/v1/zaken")
            .post()
            .body(
                mapOf(
                    "zaaktype" to zaaktype,
                    "startdatum" to startdatum.format(DATE_PATTERN),
                    "bronorganisatie" to rsin,
                    "verantwoordelijkeOrganisatie" to rsin
                )
            )
            .build()
            .execute(Zaak::class.java)
    }

    override fun getZaak(id: UUID): Zaak {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("zaken/api/v1/zaken/$id")
            .get()
            .build()
            .execute(Zaak::class.java)
    }

    override fun getZaakEigenschappen(id: UUID): Collection<Eigenschap> {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("zaken/api/v1/zaken/$id/zaakeigenschappen")
            .get()
            .build()
            .executeForCollection(Eigenschap::class.java)
    }

    override fun setZaakStatus(zaak: URI, statusType: URI, datumStatusGezet: LocalDateTime) {
        OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("zaken/api/v1/statussen")
            .post()
            .body(
                mapOf(
                    "zaak" to zaak,
                    "statustype" to statusType,
                    "datumStatusGezet" to datumStatusGezet.format(DATE_TIME_FORMAT)
                )
            )
            .build()
            .execute(Map::class.java)
    }

    override fun setZaakResultaat(zaak: URI, resultaatType: URI) {
        OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("zaken/api/v1/resultaten")
            .post()
            .body(
                mapOf(
                    "zaak" to zaak,
                    "resultaattype" to resultaatType
                )
            )
            .build()
            .execute(Map::class.java)
    }

    override fun modifyEigenschap(zaakUrl: URI, zaakId: UUID, eigenschappen: Map<URI, String>) {
        eigenschappen.forEach { (key, value) ->
            OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
                .path("zaken/api/v1/zaken/${zaakId}/zaakeigenschappen")
                .post()
                .body(
                    mapOf(
                        "zaak" to zaakUrl,
                        "eigenschap" to key,
                        "waarde" to value
                    )
                )
                .build()
                .execute(Map::class.java)
        }
    }

    class Constants {
        companion object {
            val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            val DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        }
    }

}