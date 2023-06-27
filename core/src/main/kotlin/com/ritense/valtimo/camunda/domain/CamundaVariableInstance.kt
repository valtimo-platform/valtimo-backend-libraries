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

import org.camunda.bpm.engine.impl.variable.serializer.BooleanValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.ByteArrayValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.DateValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.DefaultVariableSerializers
import org.camunda.bpm.engine.impl.variable.serializer.DoubleValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.FileValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.IntegerValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer
import org.camunda.bpm.engine.impl.variable.serializer.LongValueSerlializer
import org.camunda.bpm.engine.impl.variable.serializer.NullValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.ShortValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.StringValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
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
    private val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXECUTION_ID_")
    val execution: CamundaExecution?,

    @Column(name = "PROC_INST_ID_")
    val processInstanceId: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_DEF_ID_")
    val processDefinition: CamundaProcessDefinition?,

    @Column(name = "CASE_EXECUTION_ID_")
    val caseExecutionId: String?,

    @Column(name = "CASE_INST_ID_")
    val caseInstanceId: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TASK_ID_")
    val task: CamundaTask?,

    @Column(name = "BATCH_ID_")
    val batchId: String?,

    @Column(name = "BYTEARRAY_ID_")
    val byteArrayValueId: String?,

    @Column(name = "DOUBLE_")
    private val doubleValue: Double?,

    @Column(name = "LONG_")
    private val longValue: Long?,

    @Column(name = "TEXT_")
    private val textValue: String?,

    @Column(name = "TEXT2_")
    private val textValue2: String?,

    @Column(name = "VAR_SCOPE_")
    val variableScopeId: String?,

    @Column(name = "SEQUENCE_COUNTER_")
    val sequenceCounter: Long,

    @Column(name = "IS_CONCURRENT_LOCAL_")
    val isConcurrentLocal: Boolean,

    @Column(name = "TENANT_ID_")
    val tenantId: String?

) : ValueFields {

    override fun getName() = name

    override fun getTextValue() = textValue

    override fun setTextValue(textValue: String?) = throw RuntimeException("Can't write in an read-only interface")

    override fun getTextValue2() = textValue2

    override fun setTextValue2(textValue2: String?) = throw RuntimeException("Can't write in an read-only interface")

    override fun getLongValue() = longValue

    override fun setLongValue(longValue: Long?) = throw RuntimeException("Can't write in an read-only interface")

    override fun getDoubleValue() = doubleValue

    override fun setDoubleValue(doubleValue: Double?) = throw RuntimeException("Can't write in an read-only interface")

    override fun getByteArrayValue(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun setByteArrayValue(bytes: ByteArray?) = throw RuntimeException("Can't write in an read-only interface")

    fun getValue(): Any? = getTypedValue()?.value

    fun getTypedValue() = findSerializer(serializerName).readValue(this, true, false)

    private fun findSerializer(serializerName: String?): TypedValueSerializer<*> {
        if (serializerName == null) {
            throw RuntimeException("Failed to find serializer because name is null")
        }

        return variableSerializers.getSerializerByName(serializerName)
            ?: TODO("Not yet all serializers have been implemented. Missing serializer with name '$serializerName'")
    }

    companion object {
        val variableSerializers = DefaultVariableSerializers()

        init {
            variableSerializers.addSerializer(NullValueSerializer())
            variableSerializers.addSerializer(StringValueSerializer())
            variableSerializers.addSerializer(BooleanValueSerializer())
            variableSerializers.addSerializer(ShortValueSerializer())
            variableSerializers.addSerializer(IntegerValueSerializer())
            variableSerializers.addSerializer(LongValueSerlializer())
            variableSerializers.addSerializer(DateValueSerializer())
            variableSerializers.addSerializer(DoubleValueSerializer())
            variableSerializers.addSerializer(ByteArrayValueSerializer())
            variableSerializers.addSerializer(JavaObjectSerializer())
            variableSerializers.addSerializer(FileValueSerializer())
        }
    }

}