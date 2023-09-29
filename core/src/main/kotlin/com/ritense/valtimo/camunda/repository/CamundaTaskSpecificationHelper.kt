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

import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.KEY
import com.ritense.valtimo.camunda.repository.CamundaProcessInstanceSpecificationHelper.Companion.BUSINESS_KEY
import java.time.LocalDateTime
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.springframework.data.jpa.domain.Specification
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.Companion.ID as PROCESS_DEFINITION_ID
import com.ritense.valtimo.camunda.repository.CamundaProcessInstanceSpecificationHelper.Companion.ID as PROCESS_INSTANCE_ID

class CamundaTaskSpecificationHelper {

    companion object {

        const val ID: String = "id"
        const val REVISION: String = "revision"
        const val EXECUTION: String = "execution"
        const val PROCESS_INSTANCE: String = "processInstance"
        const val PROCESS_DEFINITION: String = "processDefinition"
        const val IDENTITY_LINKS: String = "identityLinks"
        const val CASE_EXECUTION_ID: String = "caseExecutionId"
        const val CASE_INSTANCE_ID: String = "caseInstanceId"
        const val CASE_DEFINITION_ID: String = "caseDefinitionId"
        const val NAME: String = "name"
        const val PARENT_TASK: String = "parentTask"
        const val DESCRIPTION: String = "description"
        const val TASK_DEFINITION_KEY: String = "taskDefinitionKey"
        const val OWNER: String = "owner"
        const val ASSIGNEE: String = "assignee"
        const val DELEGATION_STATE: String = "delegationState"
        const val PRIORITY: String = "priority"
        const val CREATE_TIME: String = "createTime"
        const val LAST_UPDATED: String = "lastUpdated"
        const val DUE_DATE: String = "dueDate"
        const val FOLLOW_UPDATE: String = "followUpDate"
        const val SUSPENSION_STATE: String = "suspensionState"
        const val TENANT_ID: String = "tenantId"
        const val VARIABLES: String = "variableInstances";

        @JvmStatic
        fun all() = Specification<CamundaTask> { _, _, cb ->
            cb.equal(cb.literal(1), 1)
        }

        @JvmStatic
        fun byId(taskId: String) = Specification<CamundaTask> { root, _, cb ->
            cb.equal(root.get<Any>(ID), taskId)
        }

        @JvmStatic
        fun byProcessInstanceId(processInstanceId: String) = Specification<CamundaTask> { root, _, cb ->
            cb.equal(root.get<Any>(PROCESS_INSTANCE).get<Any>(PROCESS_INSTANCE_ID), processInstanceId)
        }

        @JvmStatic
        fun byProcessInstanceBusinessKey(businessKey: String) = Specification<CamundaTask> { root, _, cb ->
            cb.equal(root.get<Any>(PROCESS_INSTANCE).get<Any>(BUSINESS_KEY), businessKey)
        }

        @JvmStatic
        fun byProcessDefinitionKeys(processDefinitionKeys: Collection<String>) =
            Specification<CamundaTask> { root, _, _ ->
                root.get<Any>(PROCESS_DEFINITION).get<Any>(KEY).`in`(processDefinitionKeys)
            }

        @JvmStatic
        fun byProcessDefinitionId(processDefinitionId: String) = Specification<CamundaTask> { root, _, cb ->
            cb.equal(root.get<Any>(PROCESS_DEFINITION).get<Any>(PROCESS_DEFINITION_ID), processDefinitionId)
        }

        @JvmStatic
        fun byCandidateGroups(candidateGroups: Collection<String>) = Specification<CamundaTask> { root, _, cb ->
            val identityLinks = root.join<Any, Any>(IDENTITY_LINKS)
            cb.and(
                cb.equal(identityLinks.get<Any>("type"), "candidate"),
                identityLinks.get<Any>("groupId").`in`(candidateGroups)
            )
        }

        @JvmStatic
        fun byCandidateGroups(vararg candidateGroups: String) = byCandidateGroups(candidateGroups.asList())

        @JvmStatic
        fun byName(taskName: String) = Specification<CamundaTask> { root, _, cb ->
            cb.equal(root.get<Any>(NAME), taskName)
        }

        @JvmStatic
        fun byAssignee(assignee: String) = Specification<CamundaTask> { root, _, cb ->
            cb.equal(root.get<Any>(ASSIGNEE), assignee)
        }

        @JvmStatic
        fun byUnassigned() = Specification<CamundaTask> { root, _, _ ->
            root.get<Any>(ASSIGNEE).isNull
        }

        @JvmStatic
        fun byAssigned() = Specification<CamundaTask> { root, _, _ ->
            root.get<Any>(ASSIGNEE).isNotNull
        }

        @JvmStatic
        fun byCreateTimeAfter(fromDate: LocalDateTime) = Specification<CamundaTask> { root, _, cb ->
            cb.greaterThan(root.get<Any>(CREATE_TIME).`as`(LocalDateTime::class.java), cb.literal(fromDate))
        }

        @JvmStatic
        fun byCreateTimeBefore(toDate: LocalDateTime) = Specification<CamundaTask> { root, _, cb ->
            cb.lessThan(root.get<Any>(CREATE_TIME).`as`(LocalDateTime::class.java), cb.literal(toDate))
        }

        @JvmStatic
        fun bySuspensionState(suspensionState: Int) = Specification<CamundaTask> { root, _, cb ->
            cb.equal(root.get<Any>(SUSPENSION_STATE), suspensionState)
        }

        @JvmStatic
        fun byActive() = bySuspensionState(SuspensionState.ACTIVE.stateCode)

    }

}