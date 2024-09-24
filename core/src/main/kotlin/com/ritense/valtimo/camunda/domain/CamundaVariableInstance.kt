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

import com.ritense.valtimo.camunda.service.CamundaContextService
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.camunda.bpm.engine.impl.variable.serializer.DefaultVariableSerializers
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields
import org.camunda.bpm.engine.variable.value.TypedValue
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "ACT_RU_VARIABLE")
class CamundaVariableInstance(

    @Id
    @Column(name = "ID_", insertable = false, updatable = false)
    val id: String,

    @Column(name = "REV_", insertable = false, updatable = false)
    val revision: Int,

    @Column(name = "TYPE_", insertable = false, updatable = false)
    val serializerName: String,

    @Column(name = "NAME_", insertable = false, updatable = false)
    private val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXECUTION_ID_", insertable = false, updatable = false)
    val execution: CamundaExecution?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_INST_ID_", insertable = false, updatable = false)
    val processInstance: CamundaExecution?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_DEF_ID_", insertable = false, updatable = false)
    val processDefinition: CamundaProcessDefinition?,

    @Column(name = "CASE_EXECUTION_ID_", insertable = false, updatable = false)
    val caseExecutionId: String?,

    @Column(name = "CASE_INST_ID_", insertable = false, updatable = false)
    val caseInstanceId: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TASK_ID_", insertable = false, updatable = false)
    val task: CamundaTask?,

    @Column(name = "BATCH_ID_", insertable = false, updatable = false)
    val batchId: String?,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BYTEARRAY_ID_", insertable = false, updatable = false)
    val byteArrayValue: CamundaBytearray?,

    @Column(name = "DOUBLE_", insertable = false, updatable = false)
    private val doubleValue: Double?,

    @Column(name = "LONG_", insertable = false, updatable = false)
    private val longValue: Long?,

    @Column(name = "TEXT_", insertable = false, updatable = false)
    private val textValue: String?,

    @Column(name = "TEXT2_", insertable = false, updatable = false)
    private val textValue2: String?,

    @Column(name = "VAR_SCOPE_", insertable = false, updatable = false)
    val variableScopeId: String?,

    @Column(name = "SEQUENCE_COUNTER_", insertable = false, updatable = false)
    val sequenceCounter: Long,

    @Column(name = "IS_CONCURRENT_LOCAL_", insertable = false, updatable = false)
    val isConcurrentLocal: Boolean,

    @Column(name = "TENANT_ID_", insertable = false, updatable = false)
    val tenantId: String?

) : ValueFields {

    override fun getName() = name

    override fun getTextValue() = textValue

    override fun setTextValue(textValue: String?) = throw RuntimeException(CANT_WRITE_READONLY_INTERFACE)

    override fun getTextValue2() = textValue2

    override fun setTextValue2(textValue2: String?) = throw RuntimeException(CANT_WRITE_READONLY_INTERFACE)

    override fun getLongValue() = longValue

    override fun setLongValue(longValue: Long?) = throw RuntimeException(CANT_WRITE_READONLY_INTERFACE)

    override fun getDoubleValue() = doubleValue

    override fun setDoubleValue(doubleValue: Double?) = throw RuntimeException(CANT_WRITE_READONLY_INTERFACE)

    @Transient
    override fun getByteArrayValue(): ByteArray? = byteArrayValue?.bytes

    override fun setByteArrayValue(bytes: ByteArray?) = throw RuntimeException(CANT_WRITE_READONLY_INTERFACE)

    @Transient
    fun getValue(): Any? = getTypedValue()?.value

    @Transient
    fun getTypedValue(): TypedValue? = getTypedValue(true)

    @Transient
    fun getTypedValue(deserializeValue: Boolean): TypedValue? {
        return CamundaContextService.runWithCommandContext {
            findSerializer(serializerName).readValue(this, deserializeValue, false)
        }
    }

    private fun findSerializer(serializerName: String?): TypedValueSerializer<*> {
        if (serializerName == null) {
            throw RuntimeException("Failed to find serializer because name is null")
        }

        return variableSerializers.getSerializerByName(serializerName)
            ?: throw RuntimeException("Failed to find serializer with name '$serializerName'")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CamundaVariableInstance) return false

        if (id != other.id) return false
        if (revision != other.revision) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + revision
        return result
    }


    companion object {
        val variableSerializers = DefaultVariableSerializers()
        private const val CANT_WRITE_READONLY_INTERFACE = "Can't write in an read-only interface"
    }
}