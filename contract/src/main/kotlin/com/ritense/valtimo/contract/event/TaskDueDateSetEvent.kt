/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.event

import com.fasterxml.jackson.annotation.JsonCreator
import com.ritense.valtimo.contract.audit.AuditEvent
import com.ritense.valtimo.contract.audit.AuditMetaData
import com.ritense.valtimo.contract.audit.ProcessIdentity
import com.ritense.valtimo.contract.audit.TaskFormerDueDate
import com.ritense.valtimo.contract.audit.TaskIdentity
import com.ritense.valtimo.contract.audit.TaskMetaData
import com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty
import com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull
import java.time.LocalDateTime
import java.util.Objects
import java.util.UUID

class TaskDueDateSetEvent @JsonCreator constructor(
    id: UUID,
    origin: String,
    occurredOn: LocalDateTime,
    user: String,
    private val formerDueDate: LocalDateTime?,
    private val dueDate: LocalDateTime?,
    private val assignee: String?,
    private val taskId: String,
    private val taskName: String,
    private val createdOn: LocalDateTime,
    private val processDefinitionId: String,
    private val processInstanceId: String,
    private val businessKey: String
) : AuditMetaData(id, origin, occurredOn, user),
    AuditEvent,
    TaskIdentity,
    TaskMetaData,
    ProcessIdentity,
    TaskFormerDueDate {

    init {
        assertArgumentNotNull(createdOn, "createdOn is required")
        assertArgumentNotNull(taskId, "taskId is required")
        assertArgumentNotNull(taskName, "taskName is required")
        assertArgumentNotNull(processDefinitionId, "processDefinitionId is required")
        assertArgumentNotNull(processInstanceId, "processInstanceId is required")
        assertArgumentNotEmpty(businessKey, "businessKey cannot be empty")
    }

    override fun getProcessDefinitionId(): String = processDefinitionId
    override fun getProcessInstanceId(): String = processInstanceId
    override fun getFormerDueDate(): LocalDateTime? = formerDueDate
    override fun getAssignee(): String? = assignee
    override fun createdOn(): LocalDateTime = createdOn
    override fun getTaskId(): String = taskId
    override fun getTaskName(): String = taskName
    override fun getBusinessKey(): String = businessKey

    override fun getDocumentId(): UUID? {
        return try {
            UUID.fromString(businessKey)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TaskAssignedEvent) return false
        if (!super.equals(other)) return false
        return taskId == other.taskId
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), taskId)
    }
}