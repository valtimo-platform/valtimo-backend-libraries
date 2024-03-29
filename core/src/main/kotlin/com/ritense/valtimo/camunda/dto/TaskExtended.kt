/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimo.camunda.dto

import com.ritense.valtimo.camunda.domain.CamundaTask
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import java.time.LocalDateTime

data class TaskExtended(
    val id: String?,
    val name: String?,
    val assignee: String?,
    val created: LocalDateTime?,
    val due: LocalDateTime?,
    val followUp: LocalDateTime?,
    val lastUpdated: LocalDateTime?,
    val delegationState: String?,
    val description: String?,
    val executionId: String?,
    val owner: String?,
    val parentTaskId: String?,
    val priority: Int,
    val processDefinitionId: String?,
    val processInstanceId: String?,
    val taskDefinitionKey: String?,
    val caseExecutionId: String?,
    val caseInstanceId: String?,
    val caseDefinitionId: String?,
    val suspended: Boolean,
    val tenantId: String?,
    val businessKey: String?,
    val processDefinitionKey: String?,
    val valtimoAssignee: ValtimoUser?,
    val context: Any?
) {

    companion object {

        @JvmStatic
        fun of(
            task: CamundaTask,
            executionId: String?,
            businessKey: String?,
            processDefinitionId: String?,
            processDefinitionKey: String?,
            valtimoAssignee: ValtimoUser?,
            context: Any?
        ) = TaskExtended(
            task.id,
            task.name,
            task.assignee,
            task.createTime,
            task.dueDate,
            task.followUpDate,
            task.lastUpdated,
            task.delegationState.toString(),
            task.description,
            executionId,
            task.owner,
            task.parentTask?.id,
            task.priority,
            processDefinitionId,
            task.getProcessInstanceId(),
            task.taskDefinitionKey,
            task.caseExecutionId,
            task.caseInstanceId,
            task.caseDefinitionId,
            task.isSuspended(),
            task.tenantId,
            businessKey,
            processDefinitionKey,
            valtimoAssignee,
            context
        )
    }
}
