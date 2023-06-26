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
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "ACT_RU_TASK")
class CamundaTask(

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "REV_")
    val revision: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXECUTION_ID_")
    val execution: CamundaExecution?,

    @Column(name = "PROC_INST_ID_")
    val processInstanceId: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_DEF_ID_")
    val processDefinition: CamundaProcessDefinition?,

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    val identityLinks: List<CamundaIdentityLink> = emptyList(),

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
    val priority: Int,

    @Column(name = "CREATE_TIME_")
    val createTime: Date?,

    @Column(name = "LAST_UPDATED_")
    val lastUpdated: Date?,

    @Column(name = "DUE_DATE_")
    val dueDate: Date?,

    @Column(name = "FOLLOW_UP_DATE_")
    val followUpDate: Date?,

    @Column(name = "SUSPENSION_STATE_")
    val suspensionState: Int,

    @Column(name = "TENANT_ID_")
    val tenantId: String?

) {
    fun isSuspended() = suspensionState == SuspensionState.SUSPENDED.stateCode
}