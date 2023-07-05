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

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "ACT_GE_BYTEARRAY")
class CamundaBytearray(

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "REV_")
    val revision: Int,

    @Column(name = "NAME_")
    val name: String?,

    @Column(name = "DEPLOYMENT_ID_")
    val deploymentId: String?,

    @Column(name = "BYTES_")
    val bytes: ByteArray?,

    @Column(name = "GENERATED_")
    val generated: Boolean?,

    @Column(name = "TENANT_ID_")
    val tenantId: String?,

    @Column(name = "TYPE_")
    val type: Int,

    @Column(name = "CREATE_TIME_")
    val createTime: LocalDateTime?,

    @Column(name = "ROOT_PROC_INST_ID_")
    val rootProcessInstanceId: String?,

    @Column(name = "REMOVAL_TIME_")
    val removalTime: LocalDateTime?

)