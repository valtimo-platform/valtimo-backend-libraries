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

package com.ritense.processdocument.domain.impl.delegate

import com.ritense.authorization.AuthorizationContext
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.authentication.UserManagementService
import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution

class DocumentDelegate(
    val processDocumentService: ProcessDocumentService,
    val userManagementService: UserManagementService,
    val documentService: DocumentService,
) {

    fun setAssignee(execution: DelegateExecution, userEmail: String?) {
        AuthorizationContext.runWithoutAuthorization {
            if (userEmail == null) {
                unassign(execution)
            }
            logger.debug("Assigning user {} to document {}", userEmail, execution.processBusinessKey)
            val documentId = processDocumentService.getDocumentId(CamundaProcessInstanceId(execution.processInstanceId), execution)
            val user = userManagementService.findByEmail(userEmail)
                .orElseThrow { IllegalArgumentException("No user found with email: $userEmail") }
            documentService.assignUserToDocument(documentId.id, user.id)
        }
    }

    fun unassign(execution: DelegateExecution) {
        logger.debug("Unassigning user from document {}", execution.processBusinessKey)
        val documentId = processDocumentService.getDocumentId(CamundaProcessInstanceId(execution.processInstanceId), execution)
        documentService.unassignUserFromDocument(documentId.id)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
