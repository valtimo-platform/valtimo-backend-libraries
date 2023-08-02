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

package com.ritense.openzaak.listener

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.service.ZaakTypeLinkService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.extension.reactor.bus.CamundaSelector
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@CamundaSelector(type = "serviceTask", event = ExecutionListener.EVENTNAME_START)
open class BaseServiceTaskListener(
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val documentService: DocumentService<JsonSchemaDocument>,
    private val repositoryService: RepositoryService
) : ReactorExecutionListener() {

    @Transactional
    override fun notify(execution: DelegateExecution) {
        val processBusinessKey = execution.processBusinessKey
        val processDefinitionKey = repositoryService.getProcessDefinition(execution.processDefinitionId).key
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString(processBusinessKey))
        val document = AuthorizationContext.runWithoutAuthorization { documentService.findBy(documentId) }.orElseThrow()
        val zaakTypeLink = zaakTypeLinkService.get(document.definitionId().name())
        if (zaakTypeLink != null) {
            notify(execution, processDefinitionKey, document, zaakTypeLink)
        }
    }

    open fun notify(execution: DelegateExecution, processDefinitionKey: String, document: Document, zaakTypeLink: ZaakTypeLink) =
        Unit

}
