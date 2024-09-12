/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimo.camunda.domain

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "ACT_RU_EXECUTION")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
class CamundaExecution(

    @Id
    @Column(name = "ID_", insertable = false, updatable = false)
    val id: String,

    @Column(name = "REV_", insertable = false, updatable = false)
    val revision: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOT_PROC_INST_ID_", insertable = false, updatable = false)
    val rootProcessInstance: CamundaExecution?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_INST_ID_", insertable = false, updatable = false)
    var processInstance: CamundaExecution?,

    @Column(name = "BUSINESS_KEY_", insertable = false, updatable = false)
    val businessKey: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID_", insertable = false, updatable = false)
    val parent: CamundaExecution?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_DEF_ID_", insertable = false, updatable = false)
    val processDefinition: CamundaProcessDefinition?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPER_EXEC_", insertable = false, updatable = false)
    val superExecution: CamundaExecution?,

    @Column(name = "SUPER_CASE_EXEC_", insertable = false, updatable = false)
    val superCaseExecutionId: String?,

    @Column(name = "CASE_INST_ID_", insertable = false, updatable = false)
    val caseInstanceId: String?,

    @Column(name = "ACT_ID_", insertable = false, updatable = false)
    val activityId: String?,

    @Column(name = "ACT_INST_ID_", insertable = false, updatable = false)
    val activityInstanceId: String?,

    @Column(name = "IS_ACTIVE_", insertable = false, updatable = false)
    val active: Boolean,

    @Column(name = "IS_CONCURRENT_", insertable = false, updatable = false)
    val concurrent: Boolean,

    @Column(name = "IS_SCOPE_", insertable = false, updatable = false)
    val scope: Boolean,

    @Column(name = "IS_EVENT_SCOPE_", insertable = false, updatable = false)
    val eventScope: Boolean,

    @Column(name = "SUSPENSION_STATE_", insertable = false, updatable = false)
    val suspensionState: Int,

    @Column(name = "CACHED_ENT_STATE_", insertable = false, updatable = false)
    val cachedEntityState: Int,

    @Column(name = "SEQUENCE_COUNTER_", insertable = false, updatable = false)
    val sequenceCounter: Long,

    @Column(name = "TENANT_ID_", insertable = false, updatable = false)
    val tenantId: String?,

    @Immutable
    @OneToMany(mappedBy = "execution", fetch = FetchType.LAZY)
    val variableInstances: Set<CamundaVariableInstance>
) : CamundaVariableScope() {

    @Transient
    fun getProcessDefinitionId() = processDefinition!!.id

    @Transient
    fun getProcessInstanceId() = processInstance!!.id

    @Transient
    override fun getVariableInstancesLocal(): Collection<CamundaVariableInstance> = variableInstances

    @Transient
    override fun getParentVariableScope(): CamundaVariableScope? = parent

    @Transient
    override fun getVariableScopeKey() = "execution"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CamundaExecution) return false

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