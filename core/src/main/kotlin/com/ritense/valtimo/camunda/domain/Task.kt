/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.camunda.domain

import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.task.DelegationState
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Table
import org.camunda.bpm.engine.task.Task as CamundaTask

@Entity
@Table(name = "ACT_RU_TASK")
class Task(

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "REV_")
    val rev: Int?,

    @Column(name = "EXECUTION_ID_")
    val executionId: String?,

    @Column(name = "PROC_INST_ID_")
    val processInstanceId: String?,

    @Column(name = "PROC_DEF_ID_")
    val processDefinitionId: String?,

    @Column(name = "CASE_EXECUTION_ID_")
    val caseExecutionId: String?,

    @Column(name = "CASE_INST_ID_")
    val caseInstanceId: String?,

    @Column(name = "CASE_DEF_ID_")
    val caseDefinitionId: String?,

    @Column(name = "NAME_")
    val name: String?,

    @Column(name = "PARENT_TASK_ID_")
    val parentTaskId: String?,

    @Column(name = "DESCRIPTION_")
    val description: String?,

    @Column(name = "TASK_DEF_KEY_")
    val taskDefinitionKey: String?,

    @Column(name = "OWNER_")
    val owner: String?,

    @Column(name = "ASSIGNEE_")
    val assignee: String?,

    @Column(name = "DELEGATION_")
    @Enumerated(EnumType.STRING)
    val delegationState: DelegationState?,

    @Column(name = "PRIORITY_")
    val priority: Int?,

    @Column(name = "CREATE_TIME_")
    val createTime: Date,

    @Column(name = "LAST_UPDATED_")
    val lastUpdated: Date,

    @Column(name = "DUE_DATE_")
    val dueDate: Date,

    @Column(name = "FOLLOW_UP_DATE_")
    val followUpDate: Date,

    @Column(name = "SUSPENSION_STATE_")
    val suspensionState: Int,

    @Column(name = "TENANT_ID_")
    val tenantId: String

) : CamundaTask {

    override fun getId() = id

    override fun getName() = name

    override fun setName(name: String?) = throw UnsupportedOperationException()

    override fun getDescription() = description

    override fun setDescription(description: String?) = throw UnsupportedOperationException()

    override fun getPriority() = priority ?: 0

    override fun setPriority(priority: Int) = throw UnsupportedOperationException()

    override fun getOwner() = owner

    override fun setOwner(owner: String?) = throw UnsupportedOperationException()

    override fun getAssignee() = assignee

    override fun setAssignee(assignee: String?) = throw UnsupportedOperationException()

    override fun getDelegationState() = delegationState

    override fun setDelegationState(delegationState: DelegationState?) = throw UnsupportedOperationException()

    override fun getProcessInstanceId() = processInstanceId

    override fun getExecutionId() = executionId

    override fun getProcessDefinitionId() = processDefinitionId

    override fun getCaseInstanceId() = caseInstanceId

    override fun setCaseInstanceId(caseInstanceId: String?) = throw UnsupportedOperationException()

    override fun getCaseExecutionId() = caseExecutionId

    override fun getCaseDefinitionId() = caseDefinitionId

    override fun getCreateTime() = createTime

    override fun getLastUpdated() = lastUpdated

    override fun getTaskDefinitionKey() = taskDefinitionKey

    override fun getDueDate() = dueDate

    override fun setDueDate(dueDate: Date?) = throw UnsupportedOperationException()

    override fun getFollowUpDate() = followUpDate

    override fun setFollowUpDate(followUpDate: Date?) = throw UnsupportedOperationException()

    override fun delegate(userId: String?) = throw UnsupportedOperationException()

    override fun setParentTaskId(parentTaskId: String?) = throw UnsupportedOperationException()

    override fun getParentTaskId() = parentTaskId

    override fun isSuspended() = suspensionState == SuspensionState.SUSPENDED.stateCode

    override fun getFormKey() = throw UnsupportedOperationException()

    override fun getCamundaFormRef() = throw UnsupportedOperationException()

    override fun getTenantId() = tenantId

    override fun setTenantId(tenantId: String?) = throw UnsupportedOperationException()
}