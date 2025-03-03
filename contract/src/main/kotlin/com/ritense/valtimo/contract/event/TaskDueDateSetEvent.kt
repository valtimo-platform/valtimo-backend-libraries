package com.ritense.valtimo.contract.event

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.valtimo.contract.audit.AuditEvent
import com.ritense.valtimo.contract.audit.AuditMetaData
import com.ritense.valtimo.contract.audit.ProcessIdentity
import com.ritense.valtimo.contract.audit.TaskIdentity
import com.ritense.valtimo.contract.audit.TaskMetaData
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

data class TaskDueDateSetEvent @JsonCreator constructor(
    val id: UUID,
    val origin: String,
    val occurredOn: LocalDateTime,
    val user: String,
    val taskId: String,
    val taskName: String?,
    val taskCreatedOn: LocalDateTime?,
    val formerDueDate: Date?,
    val dueDate: Date,
    val processDefinitionId: String,
    val processInstanceId: String,
    val businessKey: String?,
    val assignee: String?
) : AuditMetaData(id, origin, occurredOn, user), AuditEvent, TaskIdentity, TaskMetaData, ProcessIdentity {

    init {
        require(taskId.isNotBlank()) { "taskId is required" }
        taskName?.let { require(it.isNotBlank()) { "taskName is required" } }
        require(processDefinitionId.isNotBlank()) { "processDefinitionId is required" }
        require(processInstanceId.isNotBlank()) { "processInstanceId is required" }
        businessKey?.takeIf { it.isNotBlank() }?.let {
            require(it.isNotEmpty()) { "businessKey cannot be empty" }
        }
    }

    @JsonProperty
    override fun createdOn(): LocalDateTime? = taskCreatedOn

    @JsonProperty
    override fun getAssignee(): String? = assignee

    override fun getProcessDefinitionId(): String = processDefinitionId
    override fun getProcessInstanceId(): String = processInstanceId
    override fun getTaskId(): String = taskId
    override fun getTaskName(): String? = taskName
    override fun getBusinessKey(): String? = businessKey

    override fun getDocumentId(): UUID? = businessKey?.let {
        try {
            UUID.fromString(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}