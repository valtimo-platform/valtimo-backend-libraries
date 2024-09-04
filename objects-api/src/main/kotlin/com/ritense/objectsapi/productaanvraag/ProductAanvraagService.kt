/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.productaanvraag

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.klant.service.BedrijfService
import com.ritense.klant.service.BurgerService
import com.ritense.objectsapi.domain.ProductAanvraag
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.domain.OpenZaakResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.net.URI

@Service
@SkipComponentScan
class ProductAanvraagService(
    private val processDocumentService: ProcessDocumentService,
    private val documentService: DocumentService,
    private val openNotificatieService: OpenNotificatieService,
    private val zaakRolService: ZaakRolService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val burgerService: BurgerService?,
    private val bedrijfService: BedrijfService?
) {

    fun createDossier(
        productAanvraag: ProductAanvraag,
        typeMapping: ProductAanvraagTypeMapping,
        aanvragerRolTypeUrl: URI
    ) {
        val openZaakResources = openNotificatieService.createOpenzaakResources(productAanvraag.getAllFiles())
        val document = createDocument(productAanvraag, typeMapping, openZaakResources)
        assignZaakToUser(document, productAanvraag, aanvragerRolTypeUrl)
        startProcess(document.id(), typeMapping)
    }

    private fun createDocument(
        productAanvraag: ProductAanvraag,
        typeMapping: ProductAanvraagTypeMapping,
        openZaakResources: Set<OpenZaakResource>
    ): Document {
        val newDocumentRequest = NewDocumentRequest(typeMapping.caseDefinitionKey, productAanvraag.data)
            .withResources(openZaakResources)

        val documentResult = AuthorizationContext
            .runWithoutAuthorization { documentService.createDocument(newDocumentRequest) }

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
        val startProcessRequest = StartProcessForDocumentRequest(documentId, typeMapping.processDefinitionKey, null)

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
        if (!productAanvraag.bsn.isNullOrEmpty()) {
            val klant = burgerService?.ensureBurgerExists(productAanvraag.bsn)
            zaakRolService.addNatuurlijkPersoon(
                instanceLink.zaakInstanceUrl,
                roltoelichting,
                aanvragerRolTypeUrl,
                productAanvraag.bsn,
                klant?.url?.let { URI(it) }
            )
        }
        if (!productAanvraag.kvk.isNullOrEmpty()) {
            val klant = bedrijfService?.ensureBedrijfExists(productAanvraag.kvk)
            zaakRolService.addNietNatuurlijkPersoon(
                instanceLink.zaakInstanceUrl,
                roltoelichting,
                aanvragerRolTypeUrl,
                productAanvraag.kvk,
                klant?.url?.let { URI(it) }
            )
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

}
