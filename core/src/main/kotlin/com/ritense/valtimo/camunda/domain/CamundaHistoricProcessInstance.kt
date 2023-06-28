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

import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Transient

@Entity
@Table(name = "ACT_HI_PROCINST")
class CamundaHistoricProcessInstance(

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "PROC_INST_ID_")
    val processInstanceId: String?,

    @Column(name = "BUSINESS_KEY_")
    val businessKey: String?,

    @Column(name = "PROC_DEF_KEY_")
    val processDefinitionKey: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_DEF_ID_")
    val processDefinition: CamundaProcessDefinition?,

    @Column(name = "START_TIME_")
    val startTime: Date?,

    @Column(name = "END_TIME_")
    val endTime: Date?,

    @Column(name = "REMOVAL_TIME_")
    val removalTime: Date?,

    @Column(name = "DURATION_")
    val durationInMillis: Long?,

    @Column(name = "START_USER_ID_")
    val startUserId: String?,

    @Column(name = "START_ACT_ID_")
    val startActivityId: String?,

    @Column(name = "END_ACT_ID_")
    val endActivityId: String?,

    @Column(name = "SUPER_PROCESS_INSTANCE_ID_")
    val superProcessInstanceId: String?,

    @Column(name = "ROOT_PROC_INST_ID_")
    val rootProcessInstanceId: String?,

    @Column(name = "SUPER_CASE_INSTANCE_ID_")
    val superCaseInstanceId: String?,

    @Column(name = "CASE_INST_ID_")
    val caseInstanceId: String?,

    @Column(name = "DELETE_REASON_")
    val deleteReason: String?,

    @Column(name = "TENANT_ID_")
    val tenantId: String?,

    @Column(name = "STATE_")
    val state: String?

) {
    @Transient
    fun getProcessDefinitionId() = processDefinition!!.id
}