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
package com.ritense.processdocument.listener

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.service.CaseDefinitionService
import com.ritense.document.domain.Document
import com.ritense.document.event.DocumentAssigneeChangedEvent
import com.ritense.document.event.DocumentUnassignedEvent
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byAssigned
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byCandidateGroups
import com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.Companion.byProcessInstanceBusinessKey
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.service.CamundaTaskService
import mu.KotlinLogging
import org.springframework.context.event.EventListener

class CaseAssigneeListener(
    private val camundaTaskService: CamundaTaskService,
    private val documentService: DocumentService,
    private val caseDefinitionService: CaseDefinitionService,
    private val userManagementService: UserManagementService
) {

    @EventListener(DocumentAssigneeChangedEvent::class)
    fun updateAssigneeOnTasks(event: DocumentAssigneeChangedEvent) {
        val document: Document = runWithoutAuthorization {
            documentService.get(event.documentId.toString())
        }
        val caseSettings: CaseDefinitionSettings = caseDefinitionService.getCaseSettings(
            document.definitionId().name()
        )

        if (caseSettings.canHaveAssignee && caseSettings.autoAssignTasks) {
            val assignee = userManagementService.findById(document.assigneeId())
            val tasks = runWithoutAuthorization {
                camundaTaskService.findTasks(
                    byProcessInstanceBusinessKey(document.id().toString())
                        .and(byCandidateGroups(assignee.roles))

                )
            }.also {
                logger.debug { "Updating assignee on ${it.size} task(s)" }
            }


            tasks.forEach { task ->
                camundaTaskService.assign(
                    task.id,
                    assignee.email
                )
            }
        }
    }

    @EventListener(DocumentUnassignedEvent::class)
    fun removeAssigneeFromTasks(event: DocumentUnassignedEvent) {

        val document: Document = runWithoutAuthorization {
            documentService.get(event.documentId.toString())
        }
        val caseSettings: CaseDefinitionSettings = caseDefinitionService.getCaseSettings(
            document.definitionId().name()
        )

        if (caseSettings.canHaveAssignee && caseSettings.autoAssignTasks) {
            val tasks = runWithoutAuthorization {
                camundaTaskService.findTasks(
                    byProcessInstanceBusinessKey(document.id().toString())
                        .and(byAssigned())
                )
            }.also {
                logger.debug { "Removing assignee from ${it.size} task(s)" }
            }

            tasks.forEach { task ->
                camundaTaskService.unassign(task.id)
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}