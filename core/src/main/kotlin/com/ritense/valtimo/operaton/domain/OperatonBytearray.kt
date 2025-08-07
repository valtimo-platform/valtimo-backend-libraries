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

package com.ritense.valtimo.operaton.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.LocalDateTime

@Entity
@Immutable
@Table(name = "ACT_GE_BYTEARRAY")
class OperatonBytearray(

    @Id
    @Column(name = "ID_", insertable = false, updatable = false)
    val id: String,

    @Column(name = "REV_", insertable = false, updatable = false)
    val revision: Int,

    @Column(name = "NAME_", insertable = false, updatable = false)
    val name: String?,

    @Column(name = "DEPLOYMENT_ID_", insertable = false, updatable = false)
    val deploymentId: String?,

    @Column(name = "BYTES_", insertable = false, updatable = false)
    val bytes: ByteArray?,

    @Column(name = "GENERATED_", insertable = false, updatable = false)
    val generated: Boolean?,

    @Column(name = "TENANT_ID_", insertable = false, updatable = false)
    val tenantId: String?,

    @Column(name = "TYPE_", insertable = false, updatable = false)
    val type: Int,

    @Column(name = "CREATE_TIME_", insertable = false, updatable = false)
    val createTime: LocalDateTime?,

    @Column(name = "ROOT_PROC_INST_ID_", insertable = false, updatable = false)
    val rootProcessInstanceId: String?,

    @Column(name = "REMOVAL_TIME_", insertable = false, updatable = false)
    val removalTime: LocalDateTime?

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OperatonBytearray) return false

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