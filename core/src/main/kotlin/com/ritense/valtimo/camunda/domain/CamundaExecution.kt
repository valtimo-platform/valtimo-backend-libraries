/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Transient

@Entity
@Table(name = "ACT_RU_EXECUTION")
class CamundaExecution(

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "REV_")
    val revision: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOT_PROC_INST_ID_")
    val rootProcessInstance: CamundaExecution?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_INST_ID_")
    val processInstance: CamundaExecution?,

    @Column(name = "BUSINESS_KEY_")
    val businessKey: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID_")
    val parent: CamundaExecution?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_DEF_ID_")
    val processDefinition: CamundaProcessDefinition?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPER_EXEC_")
    val superExecution: CamundaExecution?,

    @Column(name = "SUPER_CASE_EXEC_")
    val superCaseExecutionId: String?,

    @Column(name = "CASE_INST_ID_")
    val caseInstanceId: String?,

    @Column(name = "ACT_ID_")
    val activityId: String?,

    @Column(name = "ACT_INST_ID_")
    val activityInstanceId: String?,

    @Column(name = "IS_ACTIVE_")
    val active: Boolean,

    @Column(name = "IS_CONCURRENT_")
    val concurrent: Boolean,

    @Column(name = "IS_SCOPE_")
    val scope: Boolean,

    @Column(name = "IS_EVENT_SCOPE_")
    val eventScope: Boolean,

    @Column(name = "SUSPENSION_STATE_")
    val suspensionState: Int,

    @Column(name = "CACHED_ENT_STATE_")
    val cachedEntityState: Int,

    @Column(name = "SEQUENCE_COUNTER_")
    val sequenceCounter: Long,

    @Column(name = "TENANT_ID_")
    val tenantId: String?,

    @OneToMany(mappedBy = "execution", fetch = FetchType.LAZY)
    val variableInstances: Set<CamundaVariableInstance>
) : CamundaVariableScope() {

    @Transient
    fun getProcessDefinitionId() = processDefinition!!.id

    @Transient
    fun getProcessInstanceId() = processInstance!!.id

    override fun getVariableInstancesLocal(): Collection<CamundaVariableInstance> = variableInstances

    override fun getParentVariableScope(): CamundaVariableScope? = parent

    override fun getVariableScopeKey() = "execution"
}