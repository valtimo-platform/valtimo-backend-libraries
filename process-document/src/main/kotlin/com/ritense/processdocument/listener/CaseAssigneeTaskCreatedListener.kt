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

import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.service.CaseDefinitionService
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.contract.authentication.UserManagementService
import mu.KotlinLogging
import org.camunda.bpm.engine.ActivityTypes
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.extension.reactor.bus.CamundaSelector
import org.camunda.bpm.extension.reactor.spring.listener.ReactorTaskListener
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@CamundaSelector(type = ActivityTypes.TASK_USER_TASK, event = TaskListener.EVENTNAME_CREATE)
open class CaseAssigneeTaskCreatedListener(
    private val taskService: TaskService,
    private val documentService: DocumentService,
    private val caseDefinitionService: CaseDefinitionService,
    private val userManagementService: UserManagementService
) : ReactorTaskListener() {

    override fun notify(delegateTask: DelegateTask) {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString(delegateTask.execution.businessKey))
        val document: Document? = documentService.findBy(documentId, delegateTask.tenantId).getOrNull()

        document?.run {
            val caseSettings: CaseDefinitionSettings = caseDefinitionService.getCaseSettings(
                this.definitionId().name()
            )

            if (
                caseSettings.canHaveAssignee &&
                caseSettings.autoAssignTasks &&
                !this.assigneeId().isNullOrEmpty()
            ) {
                val assignee = userManagementService.findById(this.assigneeId())

                taskService
                    .setAssignee(
                        delegateTask.id,
                        assignee.email
                    )
                    .also {
                        logger.debug { "Setting assignee for task with id ${delegateTask.id}" }
                    }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}