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

package com.ritense.valtimo.operaton.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.LocalDateTime

@Immutable
@Entity
@Table(name = "ACT_HI_TASKINST")
class OperatonHistoricTaskInstance (

    @Id
    @Column(name = "ID_", insertable = false, updatable = false)
    val id: String,

    @Column(name = "TASK_DEF_KEY_", insertable = false, updatable = false)
    val taskDefinitionKey: String?,

    @Column(name = "PROC_DEF_KEY_", insertable = false, updatable = false)
    val processDefinitionKey: String?,

    @Column(name = "PROC_DEF_ID_", insertable = false, updatable = false)
    val processDefinitionId: String?,

    @Column(name = "ROOT_PROC_INST_ID_", insertable = false, updatable = false)
    val rootProcessInstanceId: String?,

    @Column(name = "PROC_INST_ID_", insertable = false, updatable = false)
    val processInstanceId: String?,

    @Column(name = "EXECUTION_ID_", insertable = false, updatable = false)
    val executionId: String?,

    @Column(name = "CASE_DEF_KEY_", insertable = false, updatable = false)
    val caseDefinitionKey: String?,

    @Column(name = "CASE_DEF_ID_", insertable = false, updatable = false)
    val caseDefinitionId: String?,

    @Column(name = "CASE_INST_ID_", insertable = false, updatable = false)
    val caseInstanceId: String?,

    @Column(name = "CASE_EXECUTION_ID_", insertable = false, updatable = false)
    val caseExecutionId: String?,

    @Column(name = "ACT_INST_ID_", insertable = false, updatable = false)
    val activityInstanceId: String?,

    @Column(name = "NAME_", insertable = false, updatable = false)
    val name: String?,

    @Column(name = "PARENT_TASK_ID_", insertable = false, updatable = false)
    val parentTaskId: String?,

    @Column(name = "DESCRIPTION_", insertable = false, updatable = false)
    val description: String?,

    @Column(name = "OWNER_", insertable = false, updatable = false)
    val owner: String?,

    @Column(name = "ASSIGNEE_", insertable = false, updatable = false)
    val assignee: String?,

    @Column(name = "START_TIME_", insertable = false, updatable = false)
    val startTime: LocalDateTime?,

    @Column(name = "END_TIME_", insertable = false, updatable = false)
    val endTime: LocalDateTime?,

    @Column(name = "DURATION_", insertable = false, updatable = false)
    val durationInMillis: Long?,

    @Column(name = "DELETE_REASON_", insertable = false, updatable = false)
    val deleteReason: String?,

    @Column(name = "PRIORITY_", insertable = false, updatable = false)
    val priority: Int,

    @Column(name = "DUE_DATE_", insertable = false, updatable = false)
    val dueDate: LocalDateTime?,

    @Column(name = "FOLLOW_UP_DATE_", insertable = false, updatable = false)
    val followUpDate: LocalDateTime?,

    @Column(name = "TENANT_ID_", insertable = false, updatable = false)
    val tenantId: String?,

    @Column(name = "REMOVAL_TIME_", insertable = false, updatable = false)
    val removalTime: LocalDateTime?

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OperatonHistoricTaskInstance) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}