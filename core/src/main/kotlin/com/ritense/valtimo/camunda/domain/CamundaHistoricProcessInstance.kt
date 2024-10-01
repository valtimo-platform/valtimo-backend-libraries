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
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.Immutable
import java.time.LocalDateTime

@Immutable
@Entity
@Table(name = "ACT_HI_PROCINST")
class CamundaHistoricProcessInstance(

    @Id
    @Column(name = "ID_", insertable = false, updatable = false)
    val id: String,

    @Column(name = "PROC_INST_ID_", insertable = false, updatable = false)
    val processInstanceId: String?,

    @Column(name = "BUSINESS_KEY_", insertable = false, updatable = false)
    val businessKey: String?,

    @Column(name = "PROC_DEF_KEY_", insertable = false, updatable = false)
    val processDefinitionKey: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_DEF_ID_", insertable = false, updatable = false)
    val processDefinition: CamundaProcessDefinition?,

    @Column(name = "START_TIME_", insertable = false, updatable = false)
    val startTime: LocalDateTime?,

    @Column(name = "END_TIME_", insertable = false, updatable = false)
    val endTime: LocalDateTime?,

    @Column(name = "REMOVAL_TIME_", insertable = false, updatable = false)
    val removalTime: LocalDateTime?,

    @Column(name = "DURATION_", insertable = false, updatable = false)
    val durationInMillis: Long?,

    @Column(name = "START_USER_ID_", insertable = false, updatable = false)
    val startUserId: String?,

    @Column(name = "START_ACT_ID_", insertable = false, updatable = false)
    val startActivityId: String?,

    @Column(name = "END_ACT_ID_", insertable = false, updatable = false)
    val endActivityId: String?,

    @Column(name = "SUPER_PROCESS_INSTANCE_ID_", insertable = false, updatable = false)
    val superProcessInstanceId: String?,

    @Column(name = "ROOT_PROC_INST_ID_", insertable = false, updatable = false)
    val rootProcessInstanceId: String?,

    @Column(name = "SUPER_CASE_INSTANCE_ID_", insertable = false, updatable = false)
    val superCaseInstanceId: String?,

    @Column(name = "CASE_INST_ID_", insertable = false, updatable = false)
    val caseInstanceId: String?,

    @Column(name = "DELETE_REASON_", insertable = false, updatable = false)
    val deleteReason: String?,

    @Column(name = "TENANT_ID_", insertable = false, updatable = false)
    val tenantId: String?,

    @Column(name = "STATE_", insertable = false, updatable = false)
    val state: String?

) {
    @Transient
    fun getProcessDefinitionId() = processDefinition!!.id
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CamundaHistoricProcessInstance) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


}