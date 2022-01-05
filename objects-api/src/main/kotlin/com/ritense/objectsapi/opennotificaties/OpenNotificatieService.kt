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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.ritense.connector.service.ConnectorService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.klant.service.BurgerService
import com.ritense.objectsapi.domain.ProductAanvraag
import com.ritense.objectsapi.domain.request.HandleNotificationRequest
import com.ritense.objectsapi.productaanvraag.ProductAanvraagConnector
import com.ritense.objectsapi.productaanvraag.ProductAanvraagTypeMapping
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.openzaak.service.ZaakService
import com.ritense.openzaak.service.impl.model.documenten.InformatieObject
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
    val connectorService: ConnectorService,
    val openZaakResourceRepository: OpenZaakResourceRepository,
    val zaakRolService: ZaakRolService,
    val zaakInstanceLinkService: ZaakInstanceLinkService,
    val burgerService: BurgerService
) {
    fun handle(notification: HandleNotificationRequest, connectorId: String, authorizationKey: String) {
        if (notification.isCreateNotification() && !notification.isTestNotification()) {
            val connector = findConnector(connectorId, authorizationKey)
            val productAanvraagId = notification.getObjectId()
            val productAanvraag = connector.getProductAanvraag(productAanvraagId)
            val typeMapping = connector.getTypeMapping(productAanvraag.type)
            val aanvragerRolTypeUrl = connector.getAanvragerRolTypeUrl()

            //TODO: TP32743 Redflag needs to be refactored ASAP
            (productAanvraag.data as ObjectNode).set<TextNode>("\$bsn", TextNode(productAanvraag.bsn))

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
        val document = createDocument(productAanvraag, typeMapping, informatieObjecten)
        assignZaakToUser(document, productAanvraag, aanvragerRolTypeUrl)
        startProcess(document.id(), typeMapping)
    }

    private fun createDocument(
        productAanvraag: ProductAanvraag,
        typeMapping: ProductAanvraagTypeMapping,
        informatieObjecten: Set<InformatieObject>
    ): Document {
        val newDocumentRequest = NewDocumentRequest(typeMapping.caseDefinitionKey, productAanvraag.data)
            .withResources(getResources(informatieObjecten))

        val documentResult = documentService.createDocument(newDocumentRequest);

        if (documentResult.resultingDocument().isEmpty) {
            var logMessage = "Errors occurred during creation of dossier for productaanvraag:"
            documentResult.errors().forEach { logMessage += "\n - " + it.asString() }
            logger.error { logMessage }
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
            var logMessage = "Errors occurred during starting of process for productaanvraag:"
            processStartResult.errors().forEach { logMessage += "\n - " + it.asString() }
            logger.error { logMessage }
        }
    }

    private fun assignZaakToUser(document: Document, productAanvraag: ProductAanvraag, aanvragerRolTypeUrl: URI) {
        val instanceLink = zaakInstanceLinkService.getByDocumentId(document.id().id)
        val roltoelichting = "Aanvrager automatisch toegevoegd in GZAC"
        val klant = burgerService.createBurger(productAanvraag.bsn)
        zaakRolService.addNatuurlijkPersoon(
            instanceLink.zaakInstanceUrl,
            roltoelichting,
            aanvragerRolTypeUrl,
            productAanvraag.bsn,
            URI(klant.url)
        )
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