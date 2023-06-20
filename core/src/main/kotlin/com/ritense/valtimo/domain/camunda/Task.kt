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

package com.ritense.valtimo.domain.camunda

import org.camunda.bpm.engine.form.CamundaFormRef
import org.camunda.bpm.engine.task.DelegationState
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.camunda.bpm.engine.task.Task as CamundaTask

@Entity
@Table(name = "ACT_RU_TASK")
data class Task(

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "rev_")
    val rev: String,

    @Column(name = "execution_id_")
    val executionId: String,

    @Column(name = "proc_inst_id_")
    val processInstanceId: String,

    @Column(name = "proc_def_id_")
    val processInstanceId: String,

) : CamundaTask {

    override fun getId() = id

    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun setName(name: String?) {
        TODO("Not yet implemented")
    }

    override fun getDescription(): String {
        TODO("Not yet implemented")
    }

    override fun setDescription(description: String?) {
        TODO("Not yet implemented")
    }

    override fun getPriority(): Int {
        TODO("Not yet implemented")
    }

    override fun setPriority(priority: Int) {
        TODO("Not yet implemented")
    }

    override fun getOwner(): String {
        TODO("Not yet implemented")
    }

    override fun setOwner(owner: String?) {
        TODO("Not yet implemented")
    }

    override fun getAssignee(): String {
        TODO("Not yet implemented")
    }

    override fun setAssignee(assignee: String?) {
        TODO("Not yet implemented")
    }

    override fun getDelegationState(): DelegationState {
        TODO("Not yet implemented")
    }

    override fun setDelegationState(delegationState: DelegationState?) {
        TODO("Not yet implemented")
    }

    override fun getProcessInstanceId(): String {
        TODO("Not yet implemented")
    }

    override fun getExecutionId(): String {
        TODO("Not yet implemented")
    }

    override fun getProcessDefinitionId(): String {
        TODO("Not yet implemented")
    }

    override fun getCaseInstanceId(): String {
        TODO("Not yet implemented")
    }

    override fun setCaseInstanceId(caseInstanceId: String?) {
        TODO("Not yet implemented")
    }

    override fun getCaseExecutionId(): String {
        TODO("Not yet implemented")
    }

    override fun getCaseDefinitionId(): String {
        TODO("Not yet implemented")
    }

    override fun getCreateTime(): Date {
        TODO("Not yet implemented")
    }

    override fun getLastUpdated(): Date {
        TODO("Not yet implemented")
    }

    override fun getTaskDefinitionKey(): String {
        TODO("Not yet implemented")
    }

    override fun getDueDate(): Date {
        TODO("Not yet implemented")
    }

    override fun setDueDate(dueDate: Date?) {
        TODO("Not yet implemented")
    }

    override fun getFollowUpDate(): Date {
        TODO("Not yet implemented")
    }

    override fun setFollowUpDate(followUpDate: Date?) {
        TODO("Not yet implemented")
    }

    override fun delegate(userId: String?) {
        TODO("Not yet implemented")
    }

    override fun setParentTaskId(parentTaskId: String?) {
        TODO("Not yet implemented")
    }

    override fun getParentTaskId(): String {
        TODO("Not yet implemented")
    }

    override fun isSuspended(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getFormKey(): String {
        TODO("Not yet implemented")
    }

    override fun getCamundaFormRef(): CamundaFormRef {
        TODO("Not yet implemented")
    }

    override fun getTenantId(): String {
        TODO("Not yet implemented")
    }

    override fun setTenantId(tenantId: String?) {
        TODO("Not yet implemented")
    }
}