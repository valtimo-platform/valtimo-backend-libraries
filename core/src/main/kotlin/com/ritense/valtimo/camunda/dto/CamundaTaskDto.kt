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
import org.camunda.bpm.engine.form.CamundaFormRef
import java.util.Date

data class CamundaTaskDto(
    val id: String?,
    val name: String?,
    val assignee: String?,
    val created: Date?,
    val due: Date?,
    val followUp: Date?,
    val lastUpdated: Date?,
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
    val formKey: String?,
    val camundaFormRef: CamundaFormRef?,
    val tenantId: String?
) {

    companion object {
        fun fromEntity(task: CamundaTask) = CamundaTaskDto(
            task.id,
            task.name,
            task.assignee,
            task.createTime,
            task.dueDate,
            task.followUpDate,
            task.lastUpdated,
            task.delegationState.toString(),
            task.description,
            task.executionId,
            task.owner,
            task.parentTaskId,
            task.priority,
            task.processDefinition?.id,
            task.processInstanceId,
            task.taskDefinitionKey,
            task.caseExecutionId,
            task.caseInstanceId,
            task.caseDefinitionId,
            task.isSuspended(),
            task.getFormKey(),
            task.getCamundaFormRef(),
            task.tenantId
        )
    }
}
