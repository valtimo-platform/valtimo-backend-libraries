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

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "ACT_RU_VARIABLE")
class CamundaVariableInstance(

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "REV_")
    val revision: Int,

    @Column(name = "TYPE_")
    val serializerName: String,

    @Column(name = "NAME_")
    val name: String,

    @Column(name = "EXECUTION_ID_")
    val executionId: String?,

    @Column(name = "PROC_INST_ID_")
    val processInstanceId: String?,

    @Column(name = "PROC_DEF_ID_")
    val processDefinitionId: String?,

    @Column(name = "CASE_EXECUTION_ID_")
    val caseExecutionId: String?,

    @Column(name = "CASE_INST_ID_")
    val caseInstanceId: String?,

    @Column(name = "TASK_ID_")
    val taskId: String?,

    @Column(name = "BATCH_ID_")
    val batchId: String?,

    @Column(name = "BYTEARRAY_ID_")
    val byteArrayValueId: String?,

    @Column(name = "DOUBLE_")
    val doubleValue: Double?,

    @Column(name = "LONG_")
    val longValue: Long?,

    @Column(name = "TEXT_")
    val textValue: String?,

    @Column(name = "TEXT2_")
    val textValue2: String?,

    @Column(name = "VAR_SCOPE_")
    val variableScopeId: String?,

    @Column(name = "SEQUENCE_COUNTER_")
    val sequenceCounter: Long,

    @Column(name = "IS_CONCURRENT_LOCAL_")
    val isConcurrentLocal: Boolean,

    @Column(name = "TENANT_ID_")
    val tenantId: String?

)