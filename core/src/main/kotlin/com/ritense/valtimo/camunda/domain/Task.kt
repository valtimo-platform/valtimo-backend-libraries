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
    private val _id: String,

    @Column(name = "REV_")
    private val _rev: Int?,

    @Column(name = "EXECUTION_ID_")
    private val _executionId: String?,

    @Column(name = "PROC_INST_ID_")
    private val _processInstanceId: String?,

    @Column(name = "PROC_DEF_ID_")
    private val _processDefinitionId: String?,

    @Column(name = "CASE_EXECUTION_ID_")
    private val _caseExecutionId: String?,

    @Column(name = "CASE_INST_ID_")
    private val _caseInstanceId: String?,

    @Column(name = "CASE_DEF_ID_")
    private val _caseDefinitionId: String?,

    @Column(name = "NAME_")
    private val _name: String?,

    @Column(name = "PARENT_TASK_ID_")
    private val _parentTaskId: String?,

    @Column(name = "DESCRIPTION_")
    private val _description: String?,

    @Column(name = "TASK_DEF_KEY_")
    private val _taskDefinitionKey: String?,

    @Column(name = "OWNER_")
    private val _owner: String?,

    @Column(name = "ASSIGNEE_")
    private val _assignee: String?,

    @Column(name = "DELEGATION_")
    @Enumerated(EnumType.STRING)
    private val _delegationState: DelegationState?,

    @Column(name = "PRIORITY_")
    private val _priority: Int?,

    @Column(name = "CREATE_TIME_")
    private val _createTime: Date,

    @Column(name = "LAST_UPDATED_")
    private val _lastUpdated: Date,

    @Column(name = "DUE_DATE_")
    private val _dueDate: Date,

    @Column(name = "FOLLOW_UP_DATE_")
    private val _followUpDate: Date,

    @Column(name = "SUSPENSION_STATE_")
    private val _suspensionState: Int,

    @Column(name = "TENANT_ID_")
    private val _tenantId: String

) : CamundaTask {

    override fun getId() =  _id

    override fun getName() = _name

    override fun setName(name: String?) = throw UnsupportedOperationException()

    override fun getDescription() = _description

    override fun setDescription(description: String?) = throw UnsupportedOperationException()

    override fun getPriority() = _priority ?: 0

    override fun setPriority(priority: Int) = throw UnsupportedOperationException()

    override fun getOwner() = _owner

    override fun setOwner(owner: String?) = throw UnsupportedOperationException()

    override fun getAssignee() = _assignee

    override fun setAssignee(assignee: String?) = throw UnsupportedOperationException()

    override fun getDelegationState() = _delegationState

    override fun setDelegationState(delegationState: DelegationState?) = throw UnsupportedOperationException()

    override fun getProcessInstanceId() = _processInstanceId

    override fun getExecutionId() = _executionId

    override fun getProcessDefinitionId() = _processDefinitionId

    override fun getCaseInstanceId() = _caseInstanceId

    override fun setCaseInstanceId(caseInstanceId: String?) = throw UnsupportedOperationException()

    override fun getCaseExecutionId() = _caseExecutionId

    override fun getCaseDefinitionId() = _caseDefinitionId

    override fun getCreateTime() = _createTime

    override fun getLastUpdated() = _lastUpdated

    override fun getTaskDefinitionKey() = _taskDefinitionKey

    override fun getDueDate() = _dueDate

    override fun setDueDate(dueDate: Date?) = throw UnsupportedOperationException()

    override fun getFollowUpDate() = _followUpDate

    override fun setFollowUpDate(followUpDate: Date?) = throw UnsupportedOperationException()

    override fun delegate(userId: String?) = throw UnsupportedOperationException()

    override fun setParentTaskId(parentTaskId: String?) = throw UnsupportedOperationException()

    override fun getParentTaskId() = _parentTaskId

    override fun isSuspended() = _suspensionState == SuspensionState.SUSPENDED.stateCode

    override fun getFormKey() = throw UnsupportedOperationException()

    override fun getCamundaFormRef() = throw UnsupportedOperationException()

    override fun getTenantId() = _tenantId

    override fun setTenantId(tenantId: String?) = throw UnsupportedOperationException()
}