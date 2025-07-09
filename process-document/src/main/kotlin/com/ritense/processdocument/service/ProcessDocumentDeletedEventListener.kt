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

package com.ritense.processdocument.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.logging.withLoggingContext
import com.ritense.processdocument.domain.ProcessDocumentInstance
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId
import com.ritense.valtimo.contract.event.DocumentDeletedEvent
import com.ritense.valtimo.event.ProcessDefinitionDeleted
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.RuntimeService
import org.springframework.context.event.EventListener

class ProcessDocumentDeletedEventListener(
    private val runtimeService: RuntimeService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService
) {

    @EventListener(ProcessDefinitionDeleted::class)
    fun handle(event: ProcessDefinitionDeleted) {
        runWithoutAuthorization {
            runtimeService.createProcessInstanceQuery()
                .processDefinitionId(event.processDefinitionId)
                .rootProcessInstances()
                .list()
                .forEach {
                    deleteProcessInstance(it.processInstanceId)
                }
        }
    }

    @EventListener(DocumentDeletedEvent::class)
    fun handle(event: DocumentDeletedEvent) {
        withLoggingContext(JsonSchemaDocument::class, event.documentId) {
            logger.info { "Deleting all process instances for deleted document ${event.documentId}" }

            runWithoutAuthorization {
                runtimeService.createProcessInstanceQuery()
                    .processInstanceBusinessKey(event.documentId.toString())
                    .rootProcessInstances()
                    .list()
                    .forEach {
                        deleteProcessInstance(it.processInstanceId)
                    }
            }
        }
    }

    private fun deleteProcessInstance(processInstanceId: String) {
        runtimeService.deleteProcessInstance(
            processInstanceId,
            "Document deleted",
            true,
            true,
            true,
            false
        )
        processDocumentAssociationService.findProcessDocumentInstance(
            OperatonProcessInstanceId(processInstanceId),
        )?.let { processDocumentInstance ->
            if (processDocumentInstance.isEmpty) {
                logger.debug { "Process $processInstanceId has no relation to any Document. No ProcessDocumentInstance to delete." }
            } else {
                processDocumentInstance
                    .map(ProcessDocumentInstance::processDocumentInstanceId)
                    .ifPresent(processDocumentAssociationService::deleteProcessDocumentInstance)
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}