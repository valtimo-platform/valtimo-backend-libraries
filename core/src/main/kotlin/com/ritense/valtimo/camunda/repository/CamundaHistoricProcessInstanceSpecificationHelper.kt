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

import com.ritense.valtimo.camunda.domain.CamundaHistoricProcessInstance
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

class CamundaHistoricProcessInstanceSpecificationHelper {

    companion object {

        const val ID: String = "id"
        const val PROCESS_INSTANCE_ID: String = "processInstanceId"
        const val BUSINESS_KEY: String = "businessKey"
        const val PROCESS_DEFINITION_KEY: String = "processDefinitionKey"
        const val PROCESS_DEFINITION: String = "processDefinition"
        const val START_TIME: String = "startTime"
        const val END_TIME: String = "endTime"
        const val REMOVAL_TIME: String = "removalTime"
        const val DURATION_IN_MILLIS: String = "durationInMillis"
        const val START_USER_ID: String = "startUserId"
        const val START_ACTIVITY_ID: String = "startActivityId"
        const val END_ACTIVITY_ID: String = "endActivityId"
        const val SUPER_PROCESS_INSTANCE_ID: String = "superProcessInstanceId"
        const val ROOT_PROCESS_INSTANCE_ID: String = "rootProcessInstanceId"
        const val SUPER_CASE_INSTANCE_ID: String = "superCaseInstanceId"
        const val CASE_INSTANCE_ID: String = "caseInstanceId"
        const val DELETE_REASON: String = "deleteReason"
        const val TENANT_ID: String = "tenantId"
        const val STATE: String = "state"

        @JvmStatic
        fun query() = Specification<CamundaHistoricProcessInstance> { _, _, cb ->
            cb.equal(cb.literal(1), 1)
        }

        @JvmStatic
        fun byId(id: String) = Specification<CamundaHistoricProcessInstance> { root, _, cb ->
            cb.equal(root.get<Any>(ID), id)
        }

        @JvmStatic
        fun byProcessInstanceId(processInstanceId: String) =
            Specification<CamundaHistoricProcessInstance> { root, _, cb ->
                cb.equal(root.get<Any>(PROCESS_INSTANCE_ID), processInstanceId)
            }

        @JvmStatic
        fun byProcessDefinitionKey(processDefinitionKey: String) =
            Specification<CamundaHistoricProcessInstance> { root, _, cb ->
                cb.equal(root.get<Any>(PROCESS_DEFINITION).get<Any>("key"), processDefinitionKey)
            }

        @JvmStatic
        fun byUnfinished() = Specification<CamundaHistoricProcessInstance> { root, _, _ ->
            root.get<Any>(END_TIME).isNull
        }

        @JvmStatic
        fun byFinished() = Specification<CamundaHistoricProcessInstance> { root, _, _ ->
            root.get<Any>(END_TIME).isNotNull
        }

        @JvmStatic
        fun byEndTimeAfter(dateTime: LocalDateTime) = Specification<CamundaHistoricProcessInstance> { root, _, cb ->
            cb.greaterThanOrEqualTo(root.get<Any>(END_TIME).`as`(LocalDateTime::class.java), dateTime)
        }

        @JvmStatic
        fun byEndTimeBefore(dateTime: LocalDateTime) = Specification<CamundaHistoricProcessInstance> { root, _, cb ->
            cb.lessThanOrEqualTo(root.get<Any>(END_TIME).`as`(LocalDateTime::class.java), dateTime)
        }

        @JvmStatic
        fun byStartUserId(startUserId: String) = Specification<CamundaHistoricProcessInstance> { root, _, cb ->
            cb.equal(root.get<Any>(START_USER_ID), startUserId)
        }

    }
}