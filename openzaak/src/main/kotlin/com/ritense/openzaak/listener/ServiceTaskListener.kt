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

package com.ritense.openzaak.listener

import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.domain.mapping.impl.Operation
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.transaction.annotation.Transactional

open class ServiceTaskListener(
    private val zaakTypeLinkService: ZaakTypeLinkService,
    documentService: DocumentService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService,
    private val zaakService: ZaakService,
    repositoryService: RepositoryService
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

        when(serviceTaskHandler.operation) {
            Operation.CREATE_ZAAK -> zaakService.createZaakWithLink(execution)
            Operation.SET_RESULTAAT,
            Operation.SET_STATUS -> handleOperation(execution, processDefinitionKey, document, zaakTypeLink)
            else -> return
        }
    }

    private fun handleOperation(
        execution: DelegateExecution,
        processDefinitionKey: String,
        document: Document,
        zaakTypeLink: ZaakTypeLink
    ) {
        // TODO [RV] - Can we remove the when in handleServiceTask by using the when above?
        val zaakInstanceUrl = zaakInstanceLinkService.getByDocumentId(document.id().id).zaakInstanceUrl
        zaakTypeLink.handleServiceTask(execution, processDefinitionKey, zaakInstanceUrl)
        zaakTypeLinkService.modify(zaakTypeLink)
    }
}
