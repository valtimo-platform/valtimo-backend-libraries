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

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.service.ZaakService
import com.ritense.openzaak.service.impl.ZaakService.Constants.Companion.DATE_PATTERN
import com.ritense.openzaak.service.impl.ZaakService.Constants.Companion.DATE_TIME_FORMAT
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.Catalogus
import com.ritense.openzaak.service.impl.model.catalogi.InformatieObjectType
import com.ritense.openzaak.service.impl.model.catalogi.ResultaatType
import com.ritense.openzaak.service.impl.model.catalogi.StatusType
import com.ritense.openzaak.service.impl.model.documenten.InformatieObject
import com.ritense.openzaak.service.impl.model.zaak.Eigenschap
import com.ritense.openzaak.service.impl.model.zaak.Resultaat
import com.ritense.openzaak.service.impl.model.zaak.Status
import com.ritense.openzaak.service.impl.model.zaak.Zaak
import com.ritense.zakenapi.link.ZaakInstanceLinkService
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
    private val documentService: DocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService
) : ZaakService {

    override fun createZaakWithLink(delegateExecution: DelegateExecution) {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString(delegateExecution.processBusinessKey))
        createZaakWithLink(documentId)
    }

    override fun createZaakWithLink(documentId: Document.Id): Zaak {
        val document = AuthorizationContext.runWithoutAuthorization { documentService.findBy(documentId) }.orElseThrow()
        val openZaakConfig = openZaakConfigService.getOpenZaakConfig()!!
        val zaakTypeLink = zaakTypeLinkService.findBy(document.definitionId().name())
        val zaakInstance = createZaak(
            zaakTypeLink.zaakTypeUrl,
            LocalDateTime.now(),
            openZaakConfig.rsin.toString()
        )

        zaakInstanceLinkService.createZaakInstanceLink(
            zaakInstance.url,
            zaakInstance.uuid,
            documentId.id,
            zaakTypeLink.zaakTypeUrl
        )

        return zaakInstance
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
        val zaak = OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("zaken/api/v1/zaken/$id")
            .get()
            .build()
            .execute(Zaak::class.java)
        zaak.statusOmschrijving = zaak.status?.let { getZaakStatusOmschrijving(it) }
        zaak.resulaatOmschrijving = zaak.resultaat?.let { getZaakResultaatOmschrijving(it) }
        return zaak
    }

    override fun getZaakByDocumentId(documentId: UUID): Zaak {
        val zaakInstanceLink = zaakInstanceLinkService.getByDocumentId(documentId)
        return getZaak(zaakInstanceLink.zaakInstanceId)
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

    override fun getInformatieobjecttypes(catalogus: UUID): Collection<InformatieObjectType?> {
        val catalogusUrl = getCatalogus(catalogus).url
        return getInformatieObjectTypen(catalogusUrl).results
    }

    override fun getCatalogus(catalogus: UUID): Catalogus {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("catalogi/api/v1/catalogussen/$catalogus")
            .get()
            .build()
            .execute(Catalogus::class.java)
    }

    override fun getInformatieObjectTypen(catalogus: URI): ResultWrapper<InformatieObjectType> {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("catalogi/api/v1/informatieobjecttypen")
            .queryParams(mapOf("catalogus" to catalogus.toString()))
            .get()
            .build()
            .executeWrapped(InformatieObjectType::class.java)
    }

    override fun getInformatieObject(documentId: UUID): InformatieObject {
        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("/documenten/api/v1/enkelvoudiginformatieobjecten/$documentId")
            .get()
            .build()
            .execute(InformatieObject::class.java)
    }

    override fun getInformatieObject(file: URI): InformatieObject {
        return getInformatieObject(UUID.fromString(file.path.substringAfterLast('/')))
    }

    class Constants {
        companion object {
            val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            val DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        }
    }

    private fun getZaakStatusOmschrijving(zaakStatusUrl: URI): String? {
        val zaakStatusPath = openZaakConfigService.getOpenZaakConfig()?.let { zaakStatusUrl.toString().replace(it.url, "") }
        val statusType = zaakStatusPath?.let {
            OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
                .path(it)
                .get()
                .build()
                .execute(Status::class.java)
        }

        val statusTypePath = openZaakConfigService.getOpenZaakConfig()?.let { statusType?.statustype.toString().replace(it.url, "") }
        return statusTypePath?.let {
            OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
                .path(it)
                .get()
                .build()
                .execute(StatusType::class.java).omschrijving
        }
    }

    private fun getZaakResultaatOmschrijving(zaakResultaatUrl: URI): String? {
        val zaakResultaatPath = openZaakConfigService.getOpenZaakConfig()?.let { zaakResultaatUrl.toString().replace(it.url, "") }
        val resultaatType = zaakResultaatPath?.let {
            OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
                .path(it)
                .get()
                .build()
                .execute(Resultaat::class.java)
        }

        val resultaatTypePath = openZaakConfigService.getOpenZaakConfig()?.let { resultaatType?.resultaattype.toString().replace(it.url, "") }
        return resultaatTypePath?.let {
            OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
                .path(it)
                .get()
                .build()
                .execute(ResultaatType::class.java).omschrijving
        }
    }

}
