/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import com.ritense.besluit.connector.BesluitConnector
import com.ritense.connector.service.ConnectorService
import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.domain.mapping.impl.Operation
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.listener.BaseServiceTaskListener
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.openzaak.service.impl.ZaakInstanceLinkService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.transaction.annotation.Transactional

open class BesluitServiceTaskListener(
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val documentService: DocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val repositoryService: RepositoryService,
    private val connectorService: ConnectorService,
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
            val zaakInstanceLink = zaakInstanceLinkService.getByDocumentId(document.id().id)
            val besluitConnector = connectorService.loadByClassName(BesluitConnector::class.java)
            val besluitTypeUrl = serviceTaskHandler.parameter
            besluitConnector.createBesluit(zaakInstanceLink.zaakInstanceUrl, besluitTypeUrl)
        }
    }
}