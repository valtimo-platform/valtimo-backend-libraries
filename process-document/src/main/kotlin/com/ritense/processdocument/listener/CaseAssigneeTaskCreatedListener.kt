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

package com.ritense.processdocument.listener

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.service.CaseDefinitionService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.authentication.UserManagementService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.operaton.bpm.engine.TaskService
import org.operaton.bpm.engine.delegate.DelegateTask
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
@SkipComponentScan
open class CaseAssigneeTaskCreatedListener(
    private val taskService: TaskService,
    private val documentService: DocumentService,
    private val caseDefinitionService: CaseDefinitionService,
    private val userManagementService: UserManagementService
) {

    @EventListener(
        condition = """#delegateTask.bpmnModelElementInstance != null
            && #delegateTask.bpmnModelElementInstance.elementType.typeName == T(org.operaton.bpm.engine.ActivityTypes).TASK_USER_TASK
            && #delegateTask.eventName == T(org.operaton.bpm.engine.delegate.TaskListener).EVENTNAME_CREATE"""
    )
    fun notify(delegateTask: DelegateTask) {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString(delegateTask.execution.businessKey))
        val document: Document? = runWithoutAuthorization {
            documentService.findBy(documentId).getOrNull()
        }

        document?.run {
            val caseDefinition = runWithoutAuthorization {
                caseDefinitionService.getCaseDefinition(
                    document.definitionId().caseDefinitionId()
                )
            }

            if (caseDefinition != null) {
                if (
                    caseDefinition.canHaveAssignee
                    && caseDefinition.autoAssignTasks
                    && !this.assigneeId().isNullOrEmpty()
                ) {
                    val assignee = userManagementService.findByUsername(this.assigneeId())

                    taskService
                        .setAssignee(
                            delegateTask.id,
                            assignee.username
                        )
                        .also {
                            logger.debug { "Setting assignee for task with id ${delegateTask.id}" }
                        }
                }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}