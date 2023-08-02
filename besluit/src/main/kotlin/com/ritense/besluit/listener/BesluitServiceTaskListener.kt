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

package com.ritense.besluit.listener

import com.fasterxml.jackson.core.JsonPointer
import com.ritense.besluit.connector.BesluitConnector
import com.ritense.connector.service.ConnectorService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.domain.mapping.impl.Operation
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.listener.BaseServiceTaskListener
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.service.OpenZaakService
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.UUID

open class BesluitServiceTaskListener(
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val documentService: DocumentService<JsonSchemaDocument>,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val repositoryService: RepositoryService,
    private val connectorService: ConnectorService,
    private val openZaakService: OpenZaakService,
    private val besluitDocumentRequired: Boolean,
) : BaseServiceTaskListener(
    zaakTypeLinkService,
    documentService,
    repositoryService
) {

    @Transactional
    override fun notify(
        execution: DelegateExecution,
        processDefinitionKey: String,
        document: Document,
        zaakTypeLink: ZaakTypeLink
    ) {
        val serviceTaskId = execution.currentActivityId
        val serviceTaskHandler = zaakTypeLink.getServiceTaskHandlerBy(processDefinitionKey, serviceTaskId) ?: return

        if (serviceTaskHandler.operation == Operation.CREATE_BESLUIT) {
            createBesluit(document, serviceTaskHandler.parameter, execution.businessKey)
        }
    }

    private fun createBesluit(document: Document, besluitTypeUrl: URI, businessKey: String) {
        val besluitResource = getBesluitResource(document)

        val zaakInstanceLink = zaakInstanceLinkService.getByDocumentId(document.id().id)
        val besluitConnector = connectorService.loadByClassName(BesluitConnector::class.java)
        val besluit = besluitConnector.createBesluit(zaakInstanceLink.zaakInstanceUrl, besluitTypeUrl, businessKey)

        if (besluitResource != null) {
            besluitConnector.createBesluitInformatieobjectRelatie(besluitResource.informatieObjectUrl, besluit.url)
        }
    }

    private fun getBesluitResource(document: Document): OpenZaakResource? {
        val besluitResourceId = getBesluitOpenZaakResourceUuid(document)
        return if (besluitResourceId == null) {
            null
        } else {
            openZaakService.getResource(besluitResourceId)
        }
    }

    private fun getBesluitOpenZaakResourceUuid(document: Document): UUID? {
        val besluitResourceUuidNode = document.content().asJson().at(JsonPointer.valueOf("/besluit"))

        return if (besluitResourceUuidNode.isMissingNode || besluitResourceUuidNode.isNull) {
            if (besluitDocumentRequired) {
                throw IllegalStateException("Dossier /besluit is empty. But valtimo.besluitDocumentRequired: true")
            } else {
                null
            }
        } else {
            if (!besluitResourceUuidNode.isTextual) {
                throw RuntimeException("Dossier /besluit doesn't contain UUID: `${besluitResourceUuidNode.toPrettyString()}`")
            } else {
                try {
                    UUID.fromString(besluitResourceUuidNode.textValue())
                } catch (e: IllegalArgumentException) {
                    throw RuntimeException("Dossier /besluit contains malformed UUID", e)
                }
            }
        }
    }
}
