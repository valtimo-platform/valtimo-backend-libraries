/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.opennotificaties

import com.ritense.connector.service.ConnectorService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.domain.ProductAanvraag
import com.ritense.objectsapi.domain.request.HandleNotificationRequest
import com.ritense.objectsapi.productaanvraag.ProductAanvraagConnector
import com.ritense.objectsapi.productaanvraag.ProductAanvraagTypeMapping
import com.ritense.openzaak.service.DocumentenService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.openzaak.service.ZaakService
import com.ritense.openzaak.service.impl.model.documenten.InformatieObject
import com.ritense.openzaak.service.impl.model.zaak.Zaak
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.domain.ResourceId
import com.ritense.resource.repository.OpenZaakResourceRepository
import com.ritense.valtimo.contract.resource.Resource
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID
import mu.KotlinLogging

class OpenNotificatieService(
    val processDocumentService: ProcessDocumentService,
    val documentService: DocumentService,
    val zaakService: ZaakService,
    val documentenService: DocumentenService,
    val connectorService: ConnectorService,
    val openZaakResourceRepository: OpenZaakResourceRepository,
    val zaakRolService: ZaakRolService
) {
    fun handle(notification: HandleNotificationRequest, connectorId: String, authorizationKey: String) {
        if (notification.isCreateNotification() && !notification.isTestNotification()) {
            val connector = findConnector(connectorId, authorizationKey)
            val productAanvraagId = notification.getObjectId()
            val productAanvraag = connector.getProductAanvraag(productAanvraagId)
            val typeMapping = connector.getTypeMapping(productAanvraag.type)
            val aanvragerRolTypeUrl = connector.getAanvragerRolTypeUrl()

            createDossier(productAanvraag, typeMapping, aanvragerRolTypeUrl)
            connector.deleteProductAanvraag(notification.getObjectId())
        }
    }

    private fun findConnector(connectorId: String, authorizationKey: String): ProductAanvraagConnector {
        val connectorInstance = connectorService.getConnectorInstanceById(UUID.fromString(connectorId))
        val connector = connectorService.load(connectorInstance) as ProductAanvraagConnector
        if (!connector.verifyKey(UUID.fromString(connectorId), authorizationKey))
            throw InvalidKeyException()
        return connector
    }

    private fun createDossier(
        productAanvraag: ProductAanvraag,
        typeMapping: ProductAanvraagTypeMapping,
        aanvragerRolTypeUrl: URI
    ) {
        val informatieObjecten = getInformatieObjecten(productAanvraag.getAllFiles())
        val document = createDocumentAndProcess(productAanvraag, typeMapping, informatieObjecten)
        createZaak(productAanvraag, document, aanvragerRolTypeUrl, informatieObjecten)
        startProcess(document.id(), typeMapping)
    }

    private fun createDocumentAndProcess(
        productAanvraag: ProductAanvraag,
        typeMapping: ProductAanvraagTypeMapping,
        informatieObjecten: Set<InformatieObject>
    ): Document {
        val newDocumentRequest = NewDocumentRequest(typeMapping.caseDefinitionKey, productAanvraag.data)
            .withResources(getResources(informatieObjecten))

        val documentResult = documentService.createDocument(newDocumentRequest);

        if (documentResult.resultingDocument().isEmpty) {
            logger.error { "Errors occurred during creation of dossier for productaanvraag: ${documentResult.errors()}" }
        }

        val document = documentResult.resultingDocument().orElseThrow()

        return document
    }

    private fun startProcess(
        documentId: Document.Id,
        typeMapping: ProductAanvraagTypeMapping
    ) {
        val startProcessRequest = StartProcessForDocumentRequest(documentId, typeMapping.processDefinitionKey, null);

        val processStartResult = processDocumentService.startProcessForDocument(startProcessRequest)

        if (processStartResult.resultingDocument().isEmpty) {
            logger.error { "Errors occurred during starting of process for productaanvraag: ${processStartResult.errors()}" }
        }
    }

    private fun createZaak(
        productAanvraag: ProductAanvraag,
        document: Document,
        aanvragerRolTypeUrl: URI,
        informatieObjecten: Set<InformatieObject>
    ) {
        val zaak = zaakService.createZaakWithLink(document.id())
        assignZaakToUser(zaak, productAanvraag, aanvragerRolTypeUrl)
        linkZaakInformatieObjecten(zaak, informatieObjecten)
    }

    private fun assignZaakToUser(zaak: Zaak, productAanvraag: ProductAanvraag, aanvragerRolTypeUrl: URI) {
        val roltoelichting = "Aanvrager automatisch toegevoegd in GZAC"
        zaakRolService.addNatuurlijkPersoon(zaak, roltoelichting, aanvragerRolTypeUrl, productAanvraag.bsn)
    }

    private fun linkZaakInformatieObjecten(zaak: Zaak, informatieObjecten: Set<InformatieObject>) {
        informatieObjecten.forEach {
            documentenService.createObjectInformatieObject(it.url, zaak.url)
        }
    }

    private fun getResources(informatieObjecten: Set<InformatieObject>): Set<Resource> {
        return informatieObjecten.map {
            createOpenzaakResource(
                it.url,
                it.bestandsnaam,
                it.bestandsnaam.substringAfterLast("."),
                it.bestandsomvang
            )
        }.toCollection(hashSetOf())
    }

    private fun getInformatieObjecten(files: List<URI>): Set<InformatieObject> {
        return files.map {
            zaakService.getInformatieObject(UUID.fromString(it.path.substringAfterLast('/')))
        }.toCollection(hashSetOf())
    }

    private fun createOpenzaakResource(informatieObjectUrl: URI, name: String, extension: String, size: Long): OpenZaakResource {
        val openZaakResource = OpenZaakResource(
            ResourceId.newId(UUID.randomUUID()),
            informatieObjectUrl,
            name,
            extension,
            size,
            LocalDateTime.now()
        )
        return openZaakResourceRepository.save(openZaakResource)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}