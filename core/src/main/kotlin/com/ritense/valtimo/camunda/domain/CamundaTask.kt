/*
 *  Copyright 2015-2024 Ritense BV, the Netherlands.
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

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.camunda.bpm.engine.task.DelegationState
import org.hibernate.annotations.Immutable
import java.time.LocalDateTime

@Immutable
@Entity
@Table(name = "ACT_RU_TASK")
class CamundaTask(

    @Id
    @Column(name = "ID_", insertable = false, updatable = false)
    val id: String,

    @Column(name = "REV_", insertable = false, updatable = false)
    val revision: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXECUTION_ID_", insertable = false, updatable = false)
    val execution: CamundaExecution?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_INST_ID_", insertable = false, updatable = false)
    val processInstance: CamundaExecution?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_DEF_ID_", insertable = false, updatable = false)
    val processDefinition: CamundaProcessDefinition?,

    @Immutable
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val identityLinks: List<CamundaIdentityLink> = emptyList(),

    @Column(name = "CASE_EXECUTION_ID_", insertable = false, updatable = false)
    val caseExecutionId: String?,

    @Column(name = "CASE_INST_ID_", insertable = false, updatable = false)
    val caseInstanceId: String?,

    @Column(name = "CASE_DEF_ID_", insertable = false, updatable = false)
    val caseDefinitionId: String?,

    @Column(name = "NAME_", insertable = false, updatable = false)
    val name: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_TASK_ID_", insertable = false, updatable = false)
    val parentTask: CamundaTask?,

    @Column(name = "DESCRIPTION_", insertable = false, updatable = false)
    val description: String?,

    @Column(name = "TASK_DEF_KEY_", insertable = false, updatable = false)
    val taskDefinitionKey: String?,

    @Column(name = "OWNER_", insertable = false, updatable = false)
    val owner: String?,

    @Column(name = "ASSIGNEE_", insertable = false, updatable = false)
    val assignee: String?,

    @Column(name = "DELEGATION_", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    val delegationState: DelegationState?,

    @Column(name = "PRIORITY_", insertable = false, updatable = false)
    val priority: Int,

    @Column(name = "CREATE_TIME_", insertable = false, updatable = false)
    val createTime: LocalDateTime?,

    @Column(name = "LAST_UPDATED_", insertable = false, updatable = false)
    val lastUpdated: LocalDateTime?,

    @Column(name = "DUE_DATE_", insertable = false, updatable = false)
    val dueDate: LocalDateTime?,

    @Column(name = "FOLLOW_UP_DATE_", insertable = false, updatable = false)
    val followUpDate: LocalDateTime?,

    @Column(name = "SUSPENSION_STATE_", insertable = false, updatable = false)
    val suspensionState: Int,

    @Column(name = "TENANT_ID_", insertable = false, updatable = false)
    val tenantId: String?,

    @Immutable
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val variableInstances: Set<CamundaVariableInstance>
): CamundaVariableScope() {



    @Transient
    fun isSuspended() = suspensionState == SuspensionState.SUSPENDED.stateCode

    @Transient
    fun getProcessDefinitionId() = processDefinition!!.id

    @Transient
    fun getProcessInstanceId() = processInstance!!.id

    @Transient
    override fun getVariableInstancesLocal(): Collection<CamundaVariableInstance> = variableInstances

    @Transient
    override fun getParentVariableScope(): CamundaVariableScope? = parentTask?:execution

    @Transient
    override fun getVariableScopeKey() = "task"


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CamundaTask) return false

        if (id != other.id) return false
        if (revision != other.revision) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + revision
        return result
    }
}