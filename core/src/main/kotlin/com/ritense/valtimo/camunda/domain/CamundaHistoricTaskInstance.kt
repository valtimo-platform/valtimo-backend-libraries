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

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "ACT_HI_TASKINST")
class CamundaHistoricTaskInstance (

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "TASK_DEF_KEY_")
    val taskDefinitionKey: String?,

    @Column(name = "PROC_DEF_KEY_")
    val processDefinitionKey: String?,

    @Column(name = "PROC_DEF_ID_")
    val processDefinitionId: String?,

    @Column(name = "ROOT_PROC_INST_ID_")
    val rootProcessInstanceId: String?,

    @Column(name = "PROC_INST_ID_")
    val processInstanceId: String?,

    @Column(name = "EXECUTION_ID_")
    val executionId: String?,

    @Column(name = "CASE_DEF_KEY_")
    val caseDefinitionKey: String?,

    @Column(name = "CASE_DEF_ID_")
    val caseDefinitionId: String?,

    @Column(name = "CASE_INST_ID_")
    val caseInstanceId: String?,

    @Column(name = "CASE_EXECUTION_ID_")
    val caseExecutionId: String?,

    @Column(name = "ACT_INST_ID_")
    val activityInstanceId: String?,

    @Column(name = "NAME_")
    val name: String?,

    @Column(name = "PARENT_TASK_ID_")
    val parentTaskId: String?,

    @Column(name = "DESCRIPTION_")
    val description: String?,

    @Column(name = "OWNER_")
    val owner: String?,

    @Column(name = "ASSIGNEE_")
    val assignee: String?,

    @Column(name = "START_TIME_")
    val startTime: LocalDateTime?,

    @Column(name = "END_TIME_")
    val endTime: LocalDateTime?,

    @Column(name = "DURATION_")
    val durationInMillis: Long?,

    @Column(name = "DELETE_REASON_")
    val deleteReason: String?,

    @Column(name = "PRIORITY_")
    val priority: Int,

    @Column(name = "DUE_DATE_")
    val dueDate: LocalDateTime?,

    @Column(name = "FOLLOW_UP_DATE_")
    val followUpDate: LocalDateTime?,

    @Column(name = "TENANT_ID_")
    val tenantId: String?,

    @Column(name = "REMOVAL_TIME_")
    val removalTime: LocalDateTime?

)