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

package com.ritense.valtimo.camunda.repository

import com.ritense.valtimo.camunda.domain.CamundaVariableInstance
import org.springframework.data.jpa.domain.Specification

class CamundaVariableInstanceSpecificationHelper {

    companion object {

        const val ID: String = "id"
        const val REVISION: String = "revision"
        const val SERIALIZER_NAME: String = "serializerName"
        const val NAME: String = "name"
        const val EXECUTION: String = "execution"
        const val PROCESS_INSTANCE: String = "processInstance"
        const val PROCESS_DEFINITION: String = "processDefinition"
        const val CASE_EXECUTION_ID: String = "caseExecutionId"
        const val CASE_INSTANCE_ID: String = "caseInstanceId"
        const val TASK: String = "task"
        const val BATCH_ID: String = "batchId"
        const val BYTE_ARRAY_VALUE_ID: String = "byteArrayValueId"
        const val DOUBLE_VALUE: String = "doubleValue"
        const val LONG_VALUE: String = "longValue"
        const val TEXT_VALUE: String = "textValue"
        const val TEXT_VALUE2: String = "textValue2"
        const val VARIABLE_SCOPE_ID: String = "variableScopeId"
        const val SEQUENCE_COUNTER: String = "sequenceCounter"
        const val IS_CONCURRENT_LOCAL: String = "isConcurrentLocal"
        const val TENANT_ID: String = "tenantId"

        @JvmStatic
        fun byName(name: String) = Specification<CamundaVariableInstance> { root, _, cb ->
            cb.equal(root.get<Any>(NAME), name)
        }

        @JvmStatic
        fun byNameIn(vararg name: String) = Specification<CamundaVariableInstance> { root, _, cb ->
            if (name.isEmpty()) {
                cb.equal(cb.literal(0), 1)
            } else {
                root.get<Any>(NAME).`in`(name)
            }
        }

        @JvmStatic
        fun byProcessInstanceId(processInstanceId: String) = Specification<CamundaVariableInstance> { root, _, cb ->
            cb.equal(root.get<Any>(PROCESS_INSTANCE).get<Any>(ID), processInstanceId)
        }

        @JvmStatic
        fun byProcessInstanceIdIn(vararg processInstanceId: String) =
            Specification<CamundaVariableInstance> { root, _, _ ->
                root.get<Any>(PROCESS_INSTANCE).get<Any>(ID).`in`(processInstanceId)
            }

    }
}