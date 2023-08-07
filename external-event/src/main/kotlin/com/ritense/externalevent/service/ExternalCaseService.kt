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

package com.ritense.externalevent.service

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.externalevent.config.MappedCasesConfig
import com.ritense.externalevent.messaging.ExternalDomainMessage
import com.ritense.externalevent.messaging.`in`.CreateExternalCaseMessage
import com.ritense.externalevent.messaging.`in`.ExternalIdUpdatedConfirmationMessage
import com.ritense.externalevent.messaging.out.UpdateExternalIdPortalCaseMessage
import com.ritense.externalevent.messaging.out.UpdatePortalCaseMessage
import com.ritense.externalevent.messaging.out.UpdateStatusPortalCaseMessage
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.tenancy.TenantResolver
import mu.KotlinLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Sinks
import java.util.UUID

@Transactional
class ExternalCaseService(
    private val documentService: DocumentService,
    private val processDocumentService: ProcessDocumentService,
    private val mappedCasesConfig: MappedCasesConfig,
    private val sink: Sinks.Many<ExternalDomainMessage>,
    private val runtimeService: RuntimeService,
    private val tenantResolver: TenantResolver
) {

    /**
     * Supplier/Consumer method: Creates case in Valtimo Core,
     * and publishes message back to Portal to update the externalId.
     */
    fun createExternalCase(createExternalCaseMessage: CreateExternalCaseMessage) {
        val portalMapping = mappedCasesConfig
            .links[createExternalCaseMessage.caseDefinitionId]!!

        val newDocumentRequest = NewDocumentRequest(
            portalMapping.caseKey,
            createExternalCaseMessage.submission,
            tenantResolver.getTenantId()
        )
        val newDocumentAndStartProcessRequest = NewDocumentAndStartProcessRequest(
            portalMapping.processDefinitionKey,
            newDocumentRequest
        )
        val documentResult = processDocumentService
            .newDocumentAndStartProcess(newDocumentAndStartProcessRequest)

        if (documentResult.resultingDocument().isEmpty) {
            var logMessage = "Errors occurred during creation of external case (external caseId=${createExternalCaseMessage.caseId}, caseDefinitionId=${createExternalCaseMessage.caseDefinitionId}):"
            documentResult.errors().forEach { logMessage += "\n - " + it.asString() }
            logger.error { logMessage }
        }

        val document = documentResult.resultingDocument().orElseThrow()

        sink.tryEmitNext(
            UpdateExternalIdPortalCaseMessage(
                createExternalCaseMessage.caseId,
                document.id().toString()
            )
        )
    }

    /**
     * Supplier method: Publishes the specified status 'FreeText' back to the Portal case.
     * note this freetext should match a status in the portal otherwise it will not update.
     */
    fun publishCaseStatus(status: String, execution: DelegateExecution) {
        val externalId = getExternalId(execution)
        sink.tryEmitNext(UpdateStatusPortalCaseMessage(externalId, status))
    }

    /**
     * Supplier method: Publishes the case properties to be updated in the portal.
     * note properties should match by name otherwise it will not update.
     */
    fun publishCaseUpdate(properties: Map<JsonPointer, JsonNode>, execution: DelegateExecution) {
        val externalId = getExternalId(execution)
        sink.tryEmitNext(UpdatePortalCaseMessage(externalId, properties))
    }

    private fun getExternalId(execution: DelegateExecution): String {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString(execution.processBusinessKey))
        val document = documentService.findBy(documentId, tenantResolver.getTenantId()).orElseThrow()
        return document.id().toString()
    }

    fun getCaseValue(execution: DelegateExecution, jsonPointer: JsonPointer): JsonNode {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString(execution.processBusinessKey))
        val document = documentService.findBy(documentId, tenantResolver.getTenantId()).orElseThrow()
        return document.content().getValueBy(jsonPointer).orElseThrow()
    }

    fun processExternalIdUpdateConfirmation(externalIdUpdatedConfirmation: ExternalIdUpdatedConfirmationMessage) {
        runtimeService.createMessageCorrelation("externalIdUpdatedConfirmation")
            .processInstanceBusinessKey(externalIdUpdatedConfirmation.externalId)
            .correlateWithResult()
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}