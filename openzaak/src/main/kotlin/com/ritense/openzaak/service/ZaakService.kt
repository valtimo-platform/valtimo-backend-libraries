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

package com.ritense.openzaak.service

import com.ritense.document.domain.Document
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.Catalogus
import com.ritense.openzaak.service.impl.model.catalogi.InformatieObjectType
import com.ritense.openzaak.service.impl.model.documenten.InformatieObject
import com.ritense.openzaak.service.impl.model.zaak.Eigenschap
import com.ritense.openzaak.service.impl.model.zaak.Zaak
import org.camunda.bpm.engine.delegate.DelegateExecution
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

@Deprecated("Since 12.0.0. Replace with the Zaken API plugin")
interface ZaakService {


    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.ZakenApiPlugin.createZaak"))
    fun createZaakWithLink(delegateExecution: DelegateExecution)

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.ZakenApiPlugin.createZaak"))
    fun createZaakWithLink(documentId: Document.Id): Zaak

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.client.ZakenApiClient.createZaak"))
    fun createZaak(
        zaaktype: URI,
        startdatum: LocalDateTime,
        rsin: String,
    ): Zaak

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.client.ZakenApiClient.getZaak"))
    fun getZaak(id: UUID): Zaak

    @Deprecated("Since 12.0.0. This method will not be replaced.")
    fun getZaakByDocumentId(documentId: UUID): Zaak

    fun getZaakEigenschappen(id: UUID): Collection<Eigenschap>

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.ZakenApiPlugin.setZaakStatus"))
    fun setZaakStatus(zaak: URI, statusType: URI, datumStatusGezet: LocalDateTime)

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.zakenapi.ZakenApiPlugin.createZaakResultaat"))
    fun setZaakResultaat(zaak: URI, resultaatType: URI)

    fun modifyEigenschap(zaakUrl: URI, zaakId: UUID, eigenschappen: Map<URI, String>)

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.catalogiapi.ZakenApiPlugin.createZaakResultaat"))
    fun getInformatieobjecttypes(catalogus: UUID): Collection<InformatieObjectType?>

    @Deprecated("Since 12.0.0. This method will not be replaced.")
    fun getCatalogus(catalogus: UUID): Catalogus

    @Deprecated("Since 12.0.0. This method will not be replaced.")
    fun getInformatieObjectTypen(catalogus: URI): ResultWrapper<InformatieObjectType>

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.documentenapi.client.DocumentenApiClient.getInformatieObject"))
    fun getInformatieObject(documentId: UUID): InformatieObject

    @Deprecated("Since 12.0.0.", ReplaceWith("com.ritense.documentenapi.client.DocumentenApiClient.getInformatieObject"))
    fun getInformatieObject(file: URI): InformatieObject
}